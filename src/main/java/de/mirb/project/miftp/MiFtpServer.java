package de.mirb.project.miftp;
import de.mirb.project.miftp.fs.InMemoryFileSystem;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by mibo on 21.04.17.
 */
public class MiFtpServer {

  private final int port;
  private final BaseUser user;
  private FtpServer server;
  private FileSystemFactory fileSystemFactory;

  public MiFtpServer(int port) {
    this(port, null, null);
  }

  public MiFtpServer(int port, String username, String password) {
    if(port <= 0 || port >= 65535) {
      throw new IllegalArgumentException("Invalid port '" + port + "'");
    }
    this.port = port;

    if(username == null || password == null) {
      // create anonymous user
      user = new BaseUser();
      user.setName("anonymous");
      user.setPassword("");
      user.setHomeDirectory("/");
    } else {
      user = new BaseUser();
      user.setName(username);
      user.setPassword(password);
      user.setHomeDirectory("/");
    }
  }

  public void stop() {
    if(server != null && !server.isStopped()) {
      server.stop();
    }
  }

  public void startWithPlain() throws FtpException {
    FtpServerFactory factory = new FtpServerFactory();
    factory.setConnectionConfig(createConnectionConfig());
    factory.addListener("default", createListener(false));
    factory.setUserManager(createUserManager());
    //
    factory.setFileSystem(grantFileSystem());

    server = factory.createServer();
    server.start();
  }

  public void startWithSsl() throws FtpException {
    // configure the server
    FtpServerFactory serverFactory = new FtpServerFactory();
    serverFactory.setConnectionConfig(createConnectionConfig());
    serverFactory.addListener("default", createListener(true));
    serverFactory.setUserManager(createUserManager());
    serverFactory.setFileSystem(grantFileSystem());

    // start the server
    server = serverFactory.createServer();
    server.start();
  }

  public FileSystemView getFileSystemView(String username) {
    try {
      BaseUser user = new BaseUser();
      user.setName(username);
      return fileSystemFactory.createFileSystemView(user);
    } catch (FtpException e) {
      System.out.println("Ex: " + e.getMessage());
      // FIXME: re think exception
      throw new IllegalArgumentException("Problem with user: " + username);
    }

  }

  @NotNull
  private synchronized FileSystemFactory grantFileSystem() {
    if(fileSystemFactory == null) {
      fileSystemFactory = new InMemoryFileSystem();
    }
    return fileSystemFactory;
  }


  private ConnectionConfig createConnectionConfig() {
    ConnectionConfigFactory ccFac = new ConnectionConfigFactory();
    ccFac.setAnonymousLoginEnabled(true);
    ccFac.setMaxAnonymousLogins(5);
    return ccFac.createConnectionConfig();
  }

  private Listener createListener(boolean enableSsl) {
    ListenerFactory listenerFactory = new ListenerFactory();
    listenerFactory.setPort(port);
    if(enableSsl) {
      //    URL url = Thread.currentThread().getContextClassLoader().getResource("keystore.jks");
//    System.out.println(url.getPath());
      // define SSL configuration
      SslConfigurationFactory ssl = new SslConfigurationFactory();
      ssl.setKeystoreFile(new File("keystore.jks"));
      ssl.setKeystorePassword("password");
      // set the SSL configuration for the listener
      listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
      // XXX: After setting to false the SSL test works!? TODO: recheck
      listenerFactory.setImplicitSsl(false);
    }
    return listenerFactory.createListener();
  }

  private UserManager createUserManager() throws FtpException {
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    UserManager userManager = userManagerFactory.createUserManager();
    userManager.save(user);
//    userManagerFactory.setFile(new File("myusers.properties"));

    return userManager;
  }
}
