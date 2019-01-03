package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFsView implements FileSystemView {

  private final User user;
  private final InMemoryFileSystemConfig config;
  private final InMemoryFtpDir homeDir;
  private InMemoryFtpDir workingDir;

  public InMemoryFsView(User user, InMemoryFileSystemConfig config) {
    this.user = user;
    this.config = config;
//    workingDir = new InMemoryFtpDir("/", user);
    homeDir = new InMemoryFtpDir("/", user, config);
    workingDir = homeDir;
  }

  @Override
  public InMemoryFtpDir getHomeDirectory() throws FtpException {
    return homeDir;
  }

  @Override
  public FtpFile getWorkingDirectory() throws FtpException {
    return workingDir;
  }

  @Override
  public boolean changeWorkingDirectory(String path) throws FtpException {
    if("/".equals(path)) {
      workingDir = getHomeDirectory();
    } else {
      workingDir = new InMemoryFtpDir(workingDir, path, user, config);
    }
    return true;
  }

  @Override
  public FtpFile getFile(String name) throws FtpException {
    if(name.charAt(0) == '.') {
      if(workingDir.getName().equals(name.substring(1))) {
        return workingDir;
      }
    } else if(workingDir.getAbsolutePath().equals(name)) {
      return workingDir;
    }
    if(name.startsWith(workingDir.getAbsolutePath())) {
      // FIXME: this does not allow directories
      String canonicalPath = name.substring(workingDir.getAbsolutePath().length());
//      InMemoryFtpFile file = new InMemoryFtpFile(workingDir.getAbsolutePath(), canonicalPath, user);
//      workingDir.addFile(file);
//      return file;
      return workingDir.grantFile(canonicalPath);
    }
    return null;
  }

  @Override
  public boolean isRandomAccessible() throws FtpException {
    return false;
  }

  @Override
  public void dispose() {

  }
}
