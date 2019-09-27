package de.mirb.project;

import de.mirb.project.miftp.FileSystemConfig;
import de.mirb.project.miftp.FtpServerConfig;
import de.mirb.project.miftp.MiFtpServer;
import de.mirb.project.miftp.fs.InMemoryFileSystemConfig;
import de.mirb.project.miftp.fs.listener.FileSystemEvent;
import de.mirb.project.miftp.fs.listener.FileSystemListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.KeyManagerUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static de.mirb.project.miftp.fs.listener.FileSystemEvent.EventType.CREATED;
import static org.junit.Assert.*;

/**
 * Created by mibo on 22.04.17.
 */
@RunWith(Parameterized.class)
public class BasicServerTest {

  private MiFtpServer server;
  private String serverUrl;
  private String hostname;
  private int serverPort;
  private String user;
  private String password;

  private Map<String, List<FileSystemEvent>> user2event = new ConcurrentHashMap<>();

  private FTPClient client;

  @Parameterized.Parameter
  public boolean useSsl;

  @Parameterized.Parameters(name = "{index}: use ssl {0}")
  public static Object[] parameters() {
    return new Boolean[] {true, false};
  }

  private static final AtomicInteger USER_COUNTER = new AtomicInteger(1);

  @Before
  public void init() throws FtpException {
    serverPort = new Random().nextInt(10) + 50000;
    hostname = "localhost";
    user = "username" + USER_COUNTER.getAndIncrement();
    password = "password";

    while(!available(serverPort)) {
      serverPort++;
    }

    server = createServer(useSsl, false);
    server.start();
  }

  private final FileSystemListener listener = event -> {
    user2event.compute(event.getUser().getName(), (key, value) -> {
      if(value == null) {
        value = new ArrayList<>();
      }
      value.add(event);
      return value;
    });
  };

  private MiFtpServer createServer(boolean ssl, boolean anonymous) throws FtpException {
    FileSystemConfig fsConfig = InMemoryFileSystemConfig.with().fileSystemListener(listener).create();
    FtpServerConfig.Builder configBuilder = FtpServerConfig.with(serverPort).fileSystemConfig(fsConfig);
    if(!anonymous) {
      configBuilder.username(user).password(password);
    }
    if(ssl) {
      serverUrl = "ftps://" + user + ":" + password + "@" + hostname + ":" + serverPort + "/";
      FtpServerConfig config = configBuilder
          .keystoreName("keystore.jks").keystorePassword("password")
          .build();
      server = new MiFtpServer(config);
//      server.startWithSsl();
    } else {
      server = new MiFtpServer(configBuilder.build());
      serverUrl = "ftp://" + user + ":" + password + "@" + hostname + ":" + serverPort + "/";
//      server.startWithPlain();
    }
    return server;
  }

  @After
  public void finish() throws IOException {
    server.stop();
    if(client != null) {
      client.disconnect();
    }
  }

  @Test
  public void connectServer() throws Exception {
    // this basic connection test only works without enables ssl/ftps
    if(!useSsl) {
      URL url = new URL(serverUrl);
      URLConnection urlc = url.openConnection();
      assertNotNull(urlc.getContent());
    }
  }

  @Test
  public void accessDenied() throws Exception {
    client = createFtpClient();
    try {
      client.connect("localhost", serverPort);
      client.listDirectories();
      fail("Access MUST be denied.");
    } catch (IOException e) {
      String message = e.getMessage();
//      int reply = client.getReply();
//      assertEquals(530, reply);
      assertTrue(message, message.contains("530"));
    }
  }

  @Test
  public void anonymousLogin() throws Exception {
    server.stop();
    server = createServer(useSsl, true);
    server.start();

    FTPClient client = createFtpClient();
    client.connect("localhost", serverPort);
    client.login("anonymous", "");
    assertNotNull(client.listNames());
    client.disconnect();
  }

  @Test
  public void listFiles() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);

    client.disconnect();
  }

  @Test
  public void loginStartWithHomeDir() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    assertEquals("/", client.printWorkingDirectory());
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean mkResult = client.makeDirectory(testDirName);
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertTrue(client.changeWorkingDirectory(testDirName));
    assertEquals("/" + testDirName, client.printWorkingDirectory());
    //
    client.logout();
    client.disconnect();
    //
    client.connect(hostname, serverPort);
    client.login(user, password);

    assertEquals("/", client.printWorkingDirectory());
    client.disconnect();
  }


  @Test
  public void createDirectoryAndCwdEndingSlash() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean mkResult = client.makeDirectory(testDirName + "/");
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertEquals(testDirName, files[0].getName());
    assertTrue(client.changeWorkingDirectory(testDirName));
    files = client.listDirectories();
    assertEquals(0, files.length);
    assertTrue(client.makeDirectory("testSubDir"));
    //
    client.disconnect();
    //
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    assertTrue(client.changeWorkingDirectory("/" + testDirName + "/"));
    files = client.listDirectories();
    assertEquals(1, files.length);

    List<FileSystemEvent> fileSystemEvents = user2event.get(user);
    assertEquals(2, fileSystemEvents.size());
    assertEquals(CREATED, fileSystemEvents.get(0).getType());
    assertTrue(fileSystemEvents.stream().allMatch(f -> f.getType() == CREATED));
    assertTrue(fileSystemEvents.stream().anyMatch(f -> "/testDir/testSubDir".equals(f.getFile().getAbsolutePath())));
  }

  @Test
  public void createDirectory() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean mkResult = client.makeDirectory(testDirName);
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertEquals(testDirName, files[0].getName());
    assertTrue(client.changeWorkingDirectory(testDirName));
    files = client.listDirectories();
    assertEquals(0, files.length);
    //
    for (int i = 0; i < 10; i++) {
      String pathname = testDirName + "-" + i;
      assertTrue(client.makeDirectory(pathname));
      FTPFile[] ftpFiles = client.listDirectories();
      assertEquals(1, ftpFiles.length);
      assertEquals(pathname, ftpFiles[0].getName());
      assertTrue(client.changeWorkingDirectory(pathname));
      ftpFiles = client.listDirectories();
      assertEquals(0, ftpFiles.length);
    }
    //
    client.disconnect();
  }

  @Test
  public void createDirectoryEndingSlash() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean mkResult = client.makeDirectory(testDirName + "/");
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertEquals(testDirName, files[0].getName());
    assertTrue(client.changeWorkingDirectory(testDirName));
    files = client.listDirectories();
    assertEquals(0, files.length);
    //
    client.disconnect();
  }

  @Test
  public void createDirectoryMultiPath() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String pathOne = "first";
    String pathTwo = "second";
    boolean mkResult = client.makeDirectory(pathOne + "/" + pathTwo);
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertEquals(pathOne, files[0].getName());
    assertTrue(client.changeWorkingDirectory(pathOne));
    files = client.listDirectories();
    assertEquals(1, files.length);
    //
    client.disconnect();
  }


  /**
   * Regression test for a bug
   * @throws Exception
   */
  @Test
  public void createDirectoryAsPathOnly() throws Exception {
    FTPClient client = getVerifiedFtpClient();
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean initResult = client.changeWorkingDirectory(testDirName);
    System.out.println(client.printWorkingDirectory());
    assertFalse(initResult);
    boolean removed = client.removeDirectory(testDirName);
    assertFalse(removed);
    files = client.listDirectories();
    assertEquals(0, files.length);
    //
    String subDir = "subDir";
    assertTrue(client.makeDirectory(subDir));
    assertTrue(client.changeWorkingDirectory(subDir));
    assertTrue(client.changeWorkingDirectory("/"));
    assertTrue(client.removeDirectory(subDir));

    client.disconnect();
  }

  @Test
  public void removeDirectory() throws Exception {
    FTPClient client = getVerifiedFtpClient();
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean initResult = client.changeWorkingDirectory(testDirName);
//    System.out.println(client.printWorkingDirectory());
    assertFalse(initResult);
    boolean removed = client.removeDirectory(testDirName);
    assertFalse(removed);
    files = client.listDirectories();
    assertEquals(0, files.length);
    //
    String subDir = "subDir";
    assertTrue(client.makeDirectory(subDir));
    assertTrue(client.changeWorkingDirectory(subDir));
    assertTrue(client.changeWorkingDirectory("/"));
    assertTrue(client.removeDirectory(subDir));

    client.disconnect();
  }


  @Test
  public void removeDirectoryAndSubDirs() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);
    //
    FTPFile[] files = client.listDirectories();
    assertEquals(0, files.length);
    String testDirName = "testDir";
    boolean mkResult = client.makeDirectory(testDirName);
    assertTrue(mkResult);
    files = client.listDirectories();
    assertEquals(1, files.length);
    assertEquals(testDirName, files[0].getName());
    assertTrue(client.changeWorkingDirectory(testDirName));
    files = client.listDirectories();
    assertEquals(0, files.length);

    Stack<String> absolutePathnames = new Stack<>();
    absolutePathnames.push("/" + testDirName);
    //
    for (int i = 0; i < 10; i++) {
      String pathname = testDirName + "-" + i;
      assertTrue(client.makeDirectory(pathname));
      FTPFile[] ftpFiles = client.listDirectories();
      assertEquals(1, ftpFiles.length);
      assertEquals(pathname, ftpFiles[0].getName());
      assertTrue(client.changeWorkingDirectory(pathname));
      ftpFiles = client.listDirectories();
      assertEquals(0, ftpFiles.length);
      String before = absolutePathnames.peek();
      absolutePathnames.push(before + "/" + pathname);
    }

//    System.out.println("#################\n" + pathnames);

    client.changeWorkingDirectory("/");
    // delete
    while(!absolutePathnames.empty()) {
      String pathToRemove = absolutePathnames.pop();
//      System.out.println("Remove " + pathToRemove);
      assertTrue(client.removeDirectory(pathToRemove));
    }

    client.changeWorkingDirectory("/");
    files = client.listFiles();
    assertEquals(0, files.length);
    //
    client.disconnect();
  }

  @Test
  public void uploadFile() throws Exception {
    FTPClient client = getVerifiedFtpClient();

    // upload test file
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try(InputStream filestream = loader.getResourceAsStream("testFile.txt")) {
      boolean result = client.storeFile("/testFile.txt", filestream);
      assertTrue(result);
    }

    // verify file was uploaded
    String[] filenames = client.listNames();
    assertEquals(1, filenames.length);
    assertEquals("testFile.txt", filenames[0]);

    client.disconnect();
  }

  @Test
  public void upAndDownloadFile() throws Exception {
    FTPClient client = getVerifiedFtpClient();

    // upload test file
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try(InputStream filestream = loader.getResourceAsStream("testFile.txt")) {
      boolean result = client.storeFile("/testFile.txt", filestream);
      assertTrue(result);
    }

    try(ByteArrayOutputStream outstream = new ByteArrayOutputStream()) {
      boolean result = client.retrieveFile("/testFile.txt", outstream);
      assertTrue(result);
      byte[] content = outstream.toByteArray();

      try(InputStream filestream = loader.getResourceAsStream("testFile.txt")) {
        int size = filestream.available();
        byte[] expected = new byte[size];

        int readSize = filestream.read(expected);
        assertEquals(size, readSize);

        assertEquals(new String(expected), new String(content));
      }
    }

    client.disconnect();
  }

  @Test
  public void deleteFile() throws Exception {
    FTPClient client = getVerifiedFtpClient();
    String remotePath = "/testdir/testFile.txt";
    String localFilename = "testFile.txt";
    uploadFileTo(client, remotePath, localFilename);

    // delete

    boolean success = client.deleteFile(remotePath);
    assertTrue(success);

    // verify
    String path = extractPath(remotePath);
    client.changeWorkingDirectory(path);
    FTPFile[] files = client.listFiles();
    assertEquals("Expected no files but found: " + Arrays.toString(files), 0, files.length);

    client.disconnect();
  }

  /**
   * Get path before last `/`.
   * @param remotePath
   * @return
   */
  private String extractPath(String remotePath) {
    List<String> all = Arrays.asList(remotePath.split("/"));
    return String.join("/", all.subList(0, all.size()-1));
  }

  /**
   * Get path after last `/` (which is normally the filename).
   * @param path
   * @return
   */
  private String extractFilename(String path) {
    List<String> all = Arrays.asList(path.split("/"));
    return all.get(all.size()-1);
  }


  /*
   * Below are NO tests only supporting methods
   */



  private void uploadFileTo(FTPClient client, String remotePath, String testFilename) throws IOException {
    // upload test file
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try (InputStream filestream = loader.getResourceAsStream(testFilename)) {
      boolean result = client.storeFile(remotePath, filestream);
      assertTrue(result);
    }

//    Optional<String> remoteFilename = Arrays.stream(remotePath.split("/"))
//        .reduce((acc, in) -> in);

    String[] pathElements = remotePath.split("/");
    String remoteFilename = pathElements[pathElements.length-1];
    //

//    Arrays.stream(pathElements).forEach(noException(p -> client.changeWorkingDirectory(p)));
    Arrays.stream(pathElements).forEach(p -> {
      try {
        client.changeWorkingDirectory(p);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // verify file was uploaded
    String[] filenames = client.listNames();
    assertEquals(1, filenames.length);
    assertEquals(remoteFilename, filenames[0]);
  }

  private <T, R> Function<T, R> noException(Function<T, R> in) {
    return para -> {
      try {
        return in.apply(para);
      } catch (Exception e) {
        //
        throw new RuntimeException(e);
      }
    };
  }

//
//  @FunctionalInterface
//  public interface FunctionWithException<T, R, E extends Exception> {
//
//    R apply(T t) throws E;
//  }
//  private <T, R, E extends Exception>
//  Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
//    return arg -> {
//      try {
//        return fe.apply(arg);
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
//    };
//  }

  /**
   * Create client and verify connection before returning it.
   *
   * @return the client
   * @throws Exception if something goes wrong
   */
  private FTPClient getVerifiedFtpClient() throws Exception {
    FTPClient client = createFtpClient();
    client.connect(hostname, serverPort);
    client.login(user, password);

    // check empty root dir
    assertEquals(0, client.listDirectories().length);
    assertEquals(0, client.listNames().length);

    return client;
  }

  private FTPClient createFtpClient() throws IOException, GeneralSecurityException {
    FTPClient client;
    if(useSsl) {
      FTPSClient sclient = new FTPSClient(false);
      URL url = Thread.currentThread().getContextClassLoader().getResource("keystore.jks");
      KeyManager keyManager = KeyManagerUtils.createClientKeyManager(new File(url.getPath()), "password");
      sclient.setKeyManager(keyManager);
      KeyStoreFactory ks = new KeyStoreFactory();
      ks.setDataUrl(url);
      ks.setType("JKS");
      ks.setPassword("password");
      TrustManager trustManager = TrustManagerUtils.getDefaultTrustManager(ks.newInstance());
      sclient.setTrustManager(trustManager);
//      sclient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
//      sclient.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
//      sclient.setUseClientMode(true);
      client = sclient;
    } else {
      client = new FTPClient();
    }
    client.setConnectTimeout(1000);
    return client;
  }


  private static boolean available(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("Invalid start port: " + port);
    }

    try (ServerSocket socket = new ServerSocket(port)) {
      socket.setReuseAddress(true);
    } catch (IOException e) {
      return false;
    }
    try (DatagramSocket socket = new DatagramSocket(port)) {
      socket.setReuseAddress(true);
    } catch (IOException e) {
      return false;
    }

    return true;
  }
}
