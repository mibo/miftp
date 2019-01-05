package de.mirb.project.miftp;

import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
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

  private final FtpServerConfig config;
  private final BaseUser user;
  private FtpServer server;
  private FileSystemFactory fileSystemFactory;

  public MiFtpServer(int port) {
    this(port, null, null);
  }

  public MiFtpServer(int port, String username, String password) {
    this(FtpServerConfig.with(port).username(username).password(password).build());
  }

  public MiFtpServer(FtpServerConfig config) {
    this.config = config;
    if(config.getPort() <= 0 || config.getPort() >= 65535) {
      throw new IllegalArgumentException("Invalid port '" + config.getPort() + "'");
    }

    if(config.getUsername() == null || config.getPassword() == null) {
      // create anonymous user
      user = new BaseUser();
      user.setName("anonymous");
      user.setPassword("");
      user.setHomeDirectory("/");
    } else {
      user = new BaseUser();
      user.setName(config.getUsername());
      user.setPassword(config.getPassword());
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
      fileSystemFactory = config.getFileSystemConfig().createFileSystemFactory();
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
    listenerFactory.setPort(config.getPort());
    if(config.getPasvPorts() != null) {
      DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
  //    dccFactory.setActiveEnabled(true);
      dccFactory.setPassivePorts(config.getPasvPorts());
      listenerFactory.setDataConnectionConfiguration(dccFactory.createDataConnectionConfiguration());
    }

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
