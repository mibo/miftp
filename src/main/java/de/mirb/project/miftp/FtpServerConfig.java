package de.mirb.project.miftp;

import de.mirb.project.miftp.fs.InMemoryFileSystemConfig;

public class FtpServerConfig {
  private int port;
  private String username;
  private String password;
  private FileSystemConfig fsConfig;

  public FtpServerConfig(int port, String username, String password) {
    this(port, username, password, new InMemoryFileSystemConfig());
  }

  public FtpServerConfig(int port, String username, String password, FileSystemConfig fileSystemConfig) {
    this.port = port;
    this.username = username;
    this.password = password;
    this.fsConfig = fileSystemConfig;
  }

  public FileSystemConfig getFileSystemConfig() {
    return fsConfig;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
