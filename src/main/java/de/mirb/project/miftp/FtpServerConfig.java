package de.mirb.project.miftp;

import de.mirb.project.miftp.fs.InMemoryFileSystemConfig;

public class FtpServerConfig {
  private int port;
  private String username;
  private String password;
  private String pasvPorts;
  private String pasvAddress;
  private String pasvExtAddress;
  private FileSystemConfig fsConfig;

  private FtpServerConfig() {
  }

//  public FtpServerConfig(int port, String username, String password) {
//    this(port, username, password, new InMemoryFileSystemConfig());
//  }
//
//  public FtpServerConfig(int port, String username, String password, FileSystemConfig fileSystemConfig) {
//    this.port = port;
//    this.username = username;
//    this.password = password;
//    this.fsConfig = fileSystemConfig;
//  }

  public static Builder with(int port) {
    return new Builder()
        .fileSystemConfig(new InMemoryFileSystemConfig())
        .port(port);
  }

  public FileSystemConfig getFileSystemConfig() {
    return fsConfig;
  }

  public int getPort() {
    return port;
  }

  public String getPasvPorts() {
    return pasvPorts;
  }

  public String getPasvAddress() {
    return pasvAddress;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "FtpServerConfig{" +
        "port=" + port +
        ", username='" + username + '\'' +
        ", password='" + ((password == null)? "<unset>": "<***>") + '\'' +
        ", pasvPorts='" + pasvPorts + '\'' +
        ", pasvAddress='" + pasvAddress + '\'' +
        ", pasvExtAddress='" + pasvExtAddress + '\'' +
        ", fsConfig=" + fsConfig +
        '}';
  }

  public String getPasvExtAddress() {
    return pasvExtAddress;
  }


  public static final class Builder {
    private FtpServerConfig ftpServerConfig;

    private Builder() {
      ftpServerConfig = new FtpServerConfig();
    }

    public Builder port(int port) {
      ftpServerConfig.port = port;
      return this;
    }

    public Builder pasvPorts(String ports) {
      ftpServerConfig.pasvPorts = ports;
      return this;
    }

    public Builder pasvAddress(String address) {
      ftpServerConfig.pasvAddress = address;
      return this;
    }

    public Builder pasvExtAddress(String address) {
      ftpServerConfig.pasvExtAddress = address;
      return this;
    }

    public Builder fileSystemConfig(FileSystemConfig config) {
      ftpServerConfig.fsConfig = config;
      return this;
    }

    public Builder username(String username) {
      ftpServerConfig.username = username;
      return this;
    }

    public Builder password(String password) {
      ftpServerConfig.password = password;
      return this;
    }

    public FtpServerConfig build() {
      return ftpServerConfig;
    }
  }
}
