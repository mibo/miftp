package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.util.*;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpDir extends InMemoryFtpPath {

  private final Map<String, InMemoryFtpFile> name2File = new HashMap<>();

  public InMemoryFtpDir(String name, User user) {
    super(null, name, user);
  }

  public InMemoryFtpDir(InMemoryFtpDir parentDir, String name, User user) {
    super(parentDir, name, user);
  }

  @Override
  public boolean isDirectory() {
    return true;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  private InMemoryFtpFile createFile(String name) {
    return new InMemoryFtpFile(this, name, user);
  }

  public InMemoryFtpFile grantFile(String name) {
    return name2File.computeIfAbsent(name, this::createFile);
  }

  @Override
  public List<FtpFile> listFiles() {
    return Collections.unmodifiableList(new ArrayList<>(name2File.values()));
  }
}
