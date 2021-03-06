package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.fs.listener.BasicFileSystemEvent;
import de.mirb.project.miftp.fs.listener.FileSystemEvent;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.mirb.project.miftp.fs.listener.BasicFileSystemEvent.with;

/**
 * Created by mibo on 30.09.19.
 */
public class InMemoryFsViewContext {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFsViewContext.class);

  private final User user;
  private final InMemoryFileSystemConfig config;
  private final InMemoryFtpDir homeDir;
  private ScheduledFuture<?> cleanupScheduler;

  /** Contains all current active (created and not deleted) InMemoryFtpPath instances
   *  Mapping is from `absolute path (String)` to the InMemoryFtpPath instance */
  private final Map<String, InMemoryFtpPath> name2Path = new ConcurrentHashMap<>();

  private InMemoryFsViewContext(User user, InMemoryFileSystemConfig config) {
    this.user = user;
    this.config = config;
//    workingDir = new InMemoryFtpDir("/", user);
    homeDir = new InMemoryFtpDir(this, "/");
    name2Path.put("/", homeDir);

    if(config.getCleanupInterval() > 0) {
      // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
      //        #scheduleWithFixedDelay(java.lang.Runnable,%20long,%20long,%20java.util.concurrent.TimeUnit)
      cleanupScheduler = Executors.newSingleThreadScheduledExecutor()
          .scheduleWithFixedDelay(this::cleanUpFilesystem, 0, config.getCleanupInterval(), TimeUnit.SECONDS);
    }
  }

//  private InMemoryFsViewContext instance;
  private static final Map<String, InMemoryFsViewContext> user2View = new ConcurrentHashMap<>();


  // TODO: replace sync
  public static synchronized InMemoryFsViewContext createContext(User user, InMemoryFileSystemConfig config) {
    LOG.debug("Request InMemoryFsViewContext was {}", (user2View.containsKey(user.getName())? "reused": "created"));
    return user2View.computeIfAbsent(user.getName(), (u) -> new InMemoryFsViewContext(user, config));
//      instance = new InMemoryFsViewContext(user, config);
//    }
//    return instance;
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

  /**
   * Remove all 'stale' (based on config) files managed by this FilesystemView.
   */
  @SuppressWarnings("unchecked")
  private void cleanUpFilesystem() {
    logDebug( "Run cleanup path start for '{}'.", () -> logContent(name2Path));
    final long start = System.currentTimeMillis();
    // TODO improve/reuse filtered list
    int files = (int) name2Path.values().parallelStream().filter(InMemoryFtpPath::isFile).count();
    if(config.getMaxFiles() > 0 && files > config.getMaxFiles()) {
      LOG.debug("Run cleanup path for max files '{}' with current '{}' files listed.",
          config.getMaxFiles(), files);

      // remove oldest
      List<InMemoryFtpPath> sorted = getSortedListOfFiles(
          Comparator.comparingLong(path -> path.lastModified));
      //
      int deleteCount = (int) (sorted.size() - config.getMaxFiles());
      sorted.subList(0, deleteCount).forEach(InMemoryFtpPath::forceDelete);
      //removeAndDeleteChild(getOldestFilesName());
    }
    logTrace("Run cleanup path in between #1 '{}'.", () -> logContent(name2Path));

    // check max memory size
    long maxMemoryInBytes = config.getMaxMemoryInBytes();
    if(maxMemoryInBytes > 0) {
      long currentMemoryConsumption = currentMemoryConsumption();
      LOG.debug("Run cleanup path for maxMemoryInBytes '{}' with current consumption '{}'.",
          maxMemoryInBytes, currentMemoryConsumption);

      // start deletion the oldest files to memory consume fits
      Iterator<InMemoryFtpPath> sorted = getSortedListOfFiles(
          Comparator.comparingLong(path -> path.lastModified))
          .iterator();
      while (currentMemoryConsumption > maxMemoryInBytes && sorted.hasNext()) {
        InMemoryFtpPath removed = sorted.next();
        removed.forceDelete();
        LOG.debug("Removed '{}' for '{}' bytes.", removed.getName(), removed.getSize());
        currentMemoryConsumption -= removed.getSize();
      }
    }
    logTrace("Run cleanup path in between #2 '{}'.", () -> logContent(name2Path));

    // check for old files
    long ttlInMilliseconds = config.getTtlInMilliseconds();
    if(ttlInMilliseconds > 0) {
      LOG.debug("Remove files older then '{}' milliseconds.", ttlInMilliseconds);
      removeFilesOlderThen(ttlInMilliseconds);
    }
    LOG.debug("Run cleanup files for '{}' milliseconds.", System.currentTimeMillis() - start);
    logDebug("Run cleanup path end result '{}'.", () -> logContent(name2Path));
  }

  private String logContent(Map<String, InMemoryFtpPath> name2Path) {
    if(LOG.isDebugEnabled()) {
      return "\n" + name2Path.values().stream()
          .map(path -> path.getName() + "(" + path.getAbsolutePath() + ")")
          .collect(Collectors.joining(";\n"));
    }
    return "<no debug log set>";
  }

  private List<InMemoryFtpPath> getSortedListOfFiles(Comparator<InMemoryFtpFile> comparator) {
    // remove oldest
    return name2Path.values().parallelStream()
        .filter(InMemoryFtpPath::isFile)
        .map(path -> (InMemoryFtpFile) path)
        .sorted(comparator)
        .collect(Collectors.toList()); // instead using `limit` on the sorted stream we collect and then remove (-> performance)
  }

  private void removeFilesOlderThen(long ttlInMilliseconds) {
    List<InMemoryFtpPath> toRemove = name2Path.values().parallelStream()
        .filter(InMemoryFtpPath::isFile)
        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
        .filter(InMemoryFtpPath::isRemovable)
        .peek((name) -> LOG.debug("Remove '{}' with timestamp '{}'", name.getName(), name.getLastModified()))
//        .peek((name) -> LOG.debug("Remove {}", name))
        .collect(Collectors.toList());

    toRemove.forEach(InMemoryFtpPath::forceDelete);
  }

  private long currentMemoryConsumption() {
    if(name2Path.isEmpty()) {
      return 0;
    }
    // only collect for files because directories will count the files and sub dirs
    return name2Path.values().parallelStream()
        .filter(InMemoryFtpPath::isFile)
        .collect(Collectors.summarizingLong(InMemoryFtpPath::getSize))
        .getSum();
  }



  User getUser() {
    return user;
  }

  InMemoryFileSystemConfig getConfig() {
    return config;
  }

  void updatePath(InMemoryFtpPath path) {
//    updateListener(path, FileSystemEvent.EventType.CREATED);
    LOG.debug("Updated path '{}'", path);
    name2Path.put(path.getAbsolutePath(), path);
//    LOG.debug("Paths after update: " + name2Path.toString());
  }

  void updateListener(InMemoryFtpPath path, FileSystemEvent.EventType created) {
    if (config.getFileSystemListener() != null) {
      CompletableFuture.runAsync(() -> {
        try {
          BasicFileSystemEvent event = with(created).file(path).user(user).build();
          config.getFileSystemListener().fileSystemChanged(event);
        } catch (Exception e) {
          // catch all exceptions to prevent failing FTP tasks because of errors in FileSystemListener
          LOG.warn("Exception occurred in handling of file system changed event: " + e.getMessage(), e);
        }
      });
    }
  }

  void removePath(InMemoryFtpPath path) {
    LOG.debug("Removed path '{}'", path);
    name2Path.remove(path.getAbsolutePath());
    updateListener(path, FileSystemEvent.EventType.DELETED);
//    LOG.debug("Paths after removal: " + name2Path.toString());
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
  InMemoryFtpPath grantPathFor(String absoluteParentPath, String name) throws FtpException {
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

  public InMemoryFtpDir getHomeDir() {
    return homeDir;
  }
}
