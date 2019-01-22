package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFsView implements FileSystemView {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFsView.class);

  private final User user;
  private final InMemoryFileSystemConfig config;
  private final InMemoryFtpDir homeDir;
  private InMemoryFtpDir workingDir;
  private ScheduledFuture<?> cleanupScheduler;

  public InMemoryFsView(User user, InMemoryFileSystemConfig config) {
    this.user = user;
    this.config = config;
//    workingDir = new InMemoryFtpDir("/", user);
    homeDir = new InMemoryFtpDir("/", user, config);
    workingDir = homeDir;

    if(config.getCleanupInterval() > 0) {
      cleanupScheduler = Executors.newSingleThreadScheduledExecutor()
          .scheduleAtFixedRate(homeDir::cleanUpPath, 0, config.getCleanupInterval(), TimeUnit.SECONDS);
    }
  }

  @Override
  public InMemoryFtpDir getHomeDirectory() throws FtpException {
    LOG.debug("Get home dir '{}'.", homeDir);
    return homeDir;
  }

  @Override
  public FtpFile getWorkingDirectory() throws FtpException {
    LOG.debug("Get working dir '{}'.", workingDir);
    return workingDir;
  }

  @Override
  public boolean changeWorkingDirectory(String path) throws FtpException {
    LOG.debug("Change working dir '{}' to path '{}'.", workingDir.getAbsolutePath(), path);
    if("/".equals(path)) {
      workingDir = getHomeDirectory();
    } else {
      InMemoryFtpPath p = getFile(path);
      if(p.isDirectory()) {
        workingDir = p.asDir();
      } else {
        throw new FtpException(String.format("Invalid path '%s'.", path));
      }
    }
    LOG.debug("Changed working dir to '{}'.", workingDir.getAbsolutePath());
    return true;
  }

  @Override
  public InMemoryFtpPath getFile(String name) throws FtpException {
    LOG.debug("Get file for name '{}' in workingDir '{}'.", name, workingDir.getAbsolutePath());
    if(name.startsWith("/")) {

    }
    if(name.equals("./") || name.equals(".")) {
      return workingDir;
    } else if(workingDir.getAbsolutePath().equals(name)) {
      return workingDir;
    }
//    if(name.charAt(0) == '.') {
//      if(workingDir.getName().equals(name.substring(1))) {
//        return workingDir;
//      }
//    }

    if(name.startsWith(workingDir.getAbsolutePath())) {
      // FIXME: this does not allow directories
      String canonicalPath = name.substring(workingDir.getAbsolutePath().length());
      if(canonicalPath.startsWith("/")) {
        canonicalPath = canonicalPath.substring(1);
      }
      LOG.debug("Grant path for canonical '{}' in workingDir '{}'.", canonicalPath, workingDir.getAbsolutePath());
      return workingDir.grantPath(canonicalPath);
    }
    LOG.debug("Grant path for name '{}' in workingDir '{}'.", name, workingDir.getAbsolutePath());
    return workingDir.grantPath(name);

//    if(name.startsWith(workingDir.getAbsolutePath())) {
//      // FIXME: this does not allow directories
//      String canonicalPath = name.substring(workingDir.getAbsolutePath().length());
//      return workingDir.grantPath(canonicalPath);
//    }
//    return null;
  }

  @Override
  public boolean isRandomAccessible() throws FtpException {
    return false;
  }

  @Override
  public void dispose() {

  }
}
