package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
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

  private final Map<String, InMemoryFtpPath> name2Path = new HashMap<>();

  public InMemoryFsView(User user, InMemoryFileSystemConfig config) {
    this.user = user;
    this.config = config;
//    workingDir = new InMemoryFtpDir("/", user);
    homeDir = new InMemoryFtpDir(this, "/");
    name2Path.put("/", homeDir);
    workingDir = homeDir;

    if(config.getCleanupInterval() > 0) {
      cleanupScheduler = Executors.newSingleThreadScheduledExecutor()
          .scheduleAtFixedRate(homeDir::cleanUpPath, 0, config.getCleanupInterval(), TimeUnit.SECONDS);
    }
  }

  User getUser() {
    return user;
  }

  InMemoryFileSystemConfig getConfig() {
    return config;
  }

  void updatePath(InMemoryFtpPath path) {
    LOG.debug("Updated path '{}'", path);
    name2Path.put(path.getAbsolutePath(), path);
//    LOG.debug("Paths after update: " + name2Path.toString());
  }

  void removePath(InMemoryFtpPath path) {
    LOG.debug("Remove path '{}'", path);
    name2Path.remove(path.getAbsolutePath());
//    LOG.debug("Paths after removal: " + name2Path.toString());
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
      LOG.debug("Changed to home dir '{}'.", workingDir);
    } else {
      InMemoryFtpPath p = getFile(path);
      LOG.debug("Try to change working dir to '{}'.", p);
      if(p.isDirectory()) {
//        LOG.debug("Try to change working dir to '{}'.", p);
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
    if(name.equals("./") || name.equals(".")) {
      return workingDir;
    } else if(workingDir.getAbsolutePath().equals(name)) {
      return workingDir;
    }

    if(name.charAt(0) == '/') {
      LOG.debug("Grant path for name '{}' in home directory '{}'.", name, homeDir.getAbsolutePath());
      return getFromAbsolutePath(name);
    }

    if(name.startsWith("./")) {
      name = name.substring(2);
    }
    if(name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }

    String absolutePath = workingDir.isRootDir()?
        workingDir.getAbsolutePath() + name:
        workingDir.getAbsolutePath() + "/" + name;
    LOG.debug("Grant path for absolute path '{}' in workingDir '{}'.", absolutePath, workingDir.getAbsolutePath());
    return getFromAbsolutePath(absolutePath);
  }

  private InMemoryFtpPath getFromAbsolutePath(String absolutePath) throws FtpException {
    InMemoryFtpPath foundPath = name2Path.get(absolutePath);
    if(foundPath == null) {
      int lastSlash = absolutePath.lastIndexOf('/');
      String parentDir = lastSlash == 0? "/": absolutePath.substring(0, lastSlash);
      String name = absolutePath.substring(lastSlash + 1);
      InMemoryFtpPath path = name2Path.get(parentDir);
      if(path == null) {
        String msg = "Given absolute path '" + absolutePath + "' does not exists neither parent path exists.";
        LOG.warn(msg);
        throw new FtpException(msg);
      } else if(path.isFile()) {
        String msg = "Related parent path '" + parentDir + "' is not a directory.";
        LOG.warn(msg);
        throw new FtpException(msg);
      }
      LOG.debug("Grant path for absolutePath '{}' in dir '{}'.", absolutePath, path.getAbsolutePath());
      InMemoryFtpPath createdPath = new InMemoryFtpPath(this, path.asDir(), name);
      name2Path.put(absolutePath, createdPath);
      return createdPath;
    }
    LOG.debug("Found file for absolutePath '{}' in as '{}'.", absolutePath, foundPath);
    return foundPath;
  }

  @Override
  public boolean isRandomAccessible() throws FtpException {
    return false;
  }

  @Override
  public void dispose() {

  }
}
