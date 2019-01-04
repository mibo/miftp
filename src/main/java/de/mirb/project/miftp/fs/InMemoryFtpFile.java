package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.User;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpFile extends InMemoryFtpPath {

  public InMemoryFtpFile(InMemoryFtpDir parentDir, String name, User user) {
    super(parentDir, name, user);
  }

  @Override
  public void runValidation() {
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isFile() {
    return true;
  }
}
