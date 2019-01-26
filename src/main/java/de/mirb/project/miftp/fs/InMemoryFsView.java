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
      if(path.endsWith("/")) {
        path = path.substring(0, path.length()-1);
      }
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
      return grantAbsolutePath(name);
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
    return grantAbsolutePath(absolutePath);
  }

  private InMemoryFtpPath grantAbsolutePath(String absolutePath) throws FtpException {
    if(!absolutePath.equals("/") && absolutePath.endsWith("/")) {
      absolutePath = absolutePath.substring(0, absolutePath.length()-1);
    }
    int lastSlash = absolutePath.lastIndexOf('/');
    String parentDir = lastSlash == 0 ? "/" : absolutePath.substring(0, lastSlash);
    String parentName = absolutePath.substring(lastSlash + 1);
    return grantPathFor(parentDir, parentName);
  }

  /**
   * Parent path and name.
   * Parent path without ending 'slash' and name without starting or ending 'slash'.
   * Example:
   *  - absoluteParentPath: '/absolute/Parent/Path'
   *  - name: 'myNewFolder'
   *
   * @param absoluteParentPath path for parent (ATTENTION: must NOT end with a 'slash')
   * @param name name of to be granted path (ATTENTION: must NOT end OR START with a 'slash')
   * @return new or already existing path
   */
  private InMemoryFtpPath grantPathFor(String absoluteParentPath, String name) throws FtpException {
    String absolutePath = absoluteParentPath.equals("/") ?
        absoluteParentPath + name:
        absoluteParentPath + "/" + name;
    InMemoryFtpPath granted = name2Path.get(absolutePath);

    if(granted == null) {
      InMemoryFtpPath parentPath = name2Path.get(absoluteParentPath);
      if(parentPath == null) {
        parentPath = grantAbsolutePath(absoluteParentPath);
//        int lastSlash = absoluteParentPath.lastIndexOf('/');
//        String parentDir = lastSlash == 0 ? "/" : absoluteParentPath.substring(0, lastSlash);
//        String parentName = absoluteParentPath.substring(lastSlash + 1);
//        parentPath = grantPathFor(parentDir, parentName);
      }
      if(parentPath.isFile()) {
        String msg = "Related parent path '" + parentPath + "' is not a directory.";
        LOG.warn(msg);
        throw new FtpException(msg);
      }
      //
      LOG.debug("Grant path for name '{}' in dir '{}'.", name, parentPath.getAbsolutePath());
      InMemoryFtpPath createdPath = new InMemoryFtpPath(this, parentPath.asDir(), name);
      name2Path.put(absolutePath, createdPath);
      return createdPath;
    }

    LOG.debug("Found existing path for '{}' as '{}'.", name, absolutePath);
    return granted;
  }

  @Override
  public boolean isRandomAccessible() throws FtpException {
    return false;
  }

  @Override
  public void dispose() {

  }
}
