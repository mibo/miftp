package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFsView implements FileSystemView {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFsView.class);
  private final InMemoryFsViewContext context;

  private InMemoryFtpDir workingDir;

//  public InMemoryFsView(User user, InMemoryFileSystemConfig config) {
//    workingDir = homeDir;
//  }

  InMemoryFsView(InMemoryFsViewContext context) {
    this.context = context;
    workingDir = context.getHomeDir();
  }

  public static InMemoryFsView createView(User user, InMemoryFileSystemConfig config) {
    return new InMemoryFsView(InMemoryFsViewContext.createContext(user, config));
  }

  private <T> void log(Level level, String message, Supplier<T> ... content) {
    if(level.isLessSpecificThan(Level.DEBUG)) {
      // trace
      logTrace(message, content);
    } else if(level.isLessSpecificThan(Level.INFO)) {
      // debug
      logDebug(message, content);
    }
  }

  private <T> void logTrace(String message, Supplier<T> ... suppliers) {
    if(LOG.isTraceEnabled()) {
      List<Object> contentValues = new ArrayList<>(suppliers.length);
      for (Supplier<T> supplier : suppliers) {
        contentValues.add(supplier.get());
      }
      LOG.trace(message, contentValues);
    }
  }

  private <T> void logDebug(String message, Supplier<T> ... suppliers) {
    if(LOG.isDebugEnabled()) {
      List<Object> contentValues = new ArrayList<>(suppliers.length);
      for (Supplier<T> supplier : suppliers) {
        contentValues.add(supplier.get());
      }
      LOG.debug(message, contentValues);
    }
  }


  private String logContent(Map<String, InMemoryFtpPath> name2Path) {
    if(LOG.isDebugEnabled()) {
      return "\n" + name2Path.values().stream()
          .map(path -> path.getName() + "(" + path.getAbsolutePath() + ")")
          .collect(Collectors.joining(";\n"));
    }
    return "<no debug log set>";
  }

  @Override
  public boolean isRandomAccessible() throws FtpException {
    return false;
  }

  @Override
  public void dispose() {
    LOG.debug("Dispose FSView");
    // TODO: really?
    workingDir = context.getHomeDir();
  }

  @Override
  public InMemoryFtpDir getHomeDirectory() throws FtpException {
    LOG.debug("Get home dir '{}'.", context.getHomeDir());
    return context.getHomeDir();
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
      LOG.debug("Grant path for name '{}' in home directory '{}'.", name, context.getHomeDir().getAbsolutePath());
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
    return context.grantPathFor(parentDir, parentName);
  }
}
