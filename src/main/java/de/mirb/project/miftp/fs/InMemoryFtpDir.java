package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.fs.listener.FileSystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpDir extends InMemoryFtpPath {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFtpDir.class);

  /** Contains <code>filename</code> to <code>InMemoryFtpPath</code> instance
   *  The <code>filename</code> is the name without path (see <code>inMemoryFtpPath.getName()</code>) */
  private final Map<String, InMemoryFtpPath> name2FtpPaths = new HashMap<>();
  private final InMemoryFileSystemConfig config;

  public InMemoryFtpDir(InMemoryFsView view, String name) {
    this(view, null, name);
  }

  public InMemoryFtpDir(InMemoryFsView view, InMemoryFtpDir parentDir, String name) {
    super(view, parentDir, name);
    this.config = view.getConfig();
  }

  public InMemoryFtpDir asDir() {
    return this;
  }

  public boolean isRootDir() {
    return parentDir == null;
  }

  @Override
  public boolean isDirectory() {
    return true;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean isRemovable() {
    // directory deletion is not supported yet
    return false;
  }

  @Override
  public boolean isFlushed() {
    return true;
  }

  @Override
  public boolean doesExist() {
    return true;
  }

  @Override
  public boolean mkdir() {
    // is already a directory
    return false;
  }

  @Override
  public boolean delete() {
    // TODO: implement
    return super.delete();
  }

  @Override
  public long getSize() {
    return name2FtpPaths.values().stream()
        .collect(Collectors.summarizingLong(InMemoryFtpPath::getSize)).getSum();
  }

  private InMemoryFtpPath createPath(String name) {
    return new InMemoryFtpPath(fsView, this, name);
  }

  public InMemoryFtpPath grantPath(String name) {
    if(LOG.isDebugEnabled()) {
      LOG.debug("Grant path '{}' (exists={}).", name, name2FtpPaths.containsKey(name));
    }
    return name2FtpPaths.computeIfAbsent(name, this::createPath);
  }

  /**
   * Remove all 'stale' (based on config) files in this directory and
   * invoke this method on all remaining paths (dir and file).
   */
  public void cleanUpPath() {
    LOG.debug("Run cleanup path.");
    cleanUpFiles();
    // recurse
    // TODO: only make sense if directories are supported (instead of `InMemoryFtpFile` then `InMemoryFtpPath`)
    name2FtpPaths.values().forEach(InMemoryFtpPath::cleanUpPath);
  }

  /**
   * Remove all 'stale' (based on config) files in this directory and
   * FIXME: to use global config settings does not make sense (only for current supported use case but not in general)
   */
  private void cleanUpFiles() {
    while(config.getMaxFiles() > 0 && name2FtpPaths.size() > config.getMaxFiles()) {
      LOG.debug("Run cleanup path for max files '{}' with current '{}' files listed.",
          config.getMaxFiles(), name2FtpPaths.size());
      // remove oldest
      removeAndDeleteChild(getOldestFilesName());
    }
    // check max memory size
    long maxMemoryInBytes = config.getMaxMemoryInBytes();
    if(maxMemoryInBytes > 0) {
      long currentMemoryConsumption = currentMemoryConsumption();
      LOG.debug("Run cleanup path for maxMemoryInBytes '{}' with current consumption '{}'.",
          maxMemoryInBytes, currentMemoryConsumption);

      while (currentMemoryConsumption > maxMemoryInBytes) {
        InMemoryFtpPath removed = removeAndDeleteChild(getOldestFilesName());
        LOG.debug("Removed '{}' for '{}' bytes.", removed.getName(), removed.getSize());
        currentMemoryConsumption -= removed.getSize();
      }
    }
    // check for old files
    long ttlInMilliseconds = config.getTtlInMilliseconds();
    if(ttlInMilliseconds > 0) {
      LOG.debug("Run cleanup path for ttlInMilliseconds '{}'.", ttlInMilliseconds);
      removeFilesOlderThen(ttlInMilliseconds);
    }
  }

  /**
   * Remove child from internal mapping and call <code>delete()</code> on child.
   *
   * @param name
   * @return
   */
  private InMemoryFtpPath removeAndDeleteChild(String name) {
    InMemoryFtpPath removed = name2FtpPaths.remove(name);
    if(removed == null) {
      throw new IllegalStateException(String.format("Invalid path '%s' for removal.", name));
    }
    // in this case we force the deletion
    removed.forceDelete();
    return removed;
  }


  /**
   * Remove child from internal mapping (without calling further methods)
   * @param path to be removed
   */
  void removeChildPath(InMemoryFtpPath path) {
    name2FtpPaths.remove(path.name);
  }

  private void removeFilesOlderThen(long ttlInMilliseconds) {
    List<InMemoryFtpPath> toRemove = name2FtpPaths.values().stream()
        .filter(InMemoryFtpPath::isFile)
        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
        .filter(InMemoryFtpPath::isRemovable)
        .peek((name) -> LOG.debug("Remove '{}' with timestamp '{}'", name.getName(), name.getLastModified()))
//        .peek((name) -> LOG.debug("Remove {}", name))
        .collect(Collectors.toList());

    toRemove.forEach(f -> removeAndDeleteChild(f.getName()));
  }

  private long currentMemoryConsumption() {
    if(name2FtpPaths.isEmpty()) {
      return 0;
    }
    return name2FtpPaths.values().stream()
        .collect(Collectors.summarizingLong(InMemoryFtpPath::getSize))
        .getSum();
  }

  /**
   * Oldest filename which is remove able (only for files directories are skipped).
   *
   * @return
   */
  private String getOldestFilesName() {
    if(name2FtpPaths.isEmpty()) {
      throw new IllegalStateException();
    }
    return name2FtpPaths.values().stream()
        .filter(InMemoryFtpPath::isFile)
        .filter(InMemoryFtpPath::isFlushed)
        .min((first, second) -> (int) ((int) first.getLastModified() - second.getLastModified()))
        .map(InMemoryFtpPath::getName).get();
  }

//  /**
//   * Oldest filename only for files (directories are skipped).
//   *
//   * @return
//   */
//  private String getOldestFilesName() {
//    if(name2File.isEmpty()) {
//      throw new IllegalStateException();
//    }
//    return name2File.values().stream()
//        .filter(InMemoryFtpFile::isFile)
//        .min((first, second) -> (int) ((int) first.getLastModified() - second.getLastModified()))
//        .map(InMemoryFtpPath::getName).get();
//  }

  @Override
  public List<InMemoryFtpPath> listFiles() {
    return Collections.unmodifiableList(new ArrayList<>(name2FtpPaths.values()));
  }

  public InMemoryFtpDir convertToDir(InMemoryFtpPath inMemoryFtpPath) {
    LOG.debug("Convert '{}' in dir '{}' to directory.", inMemoryFtpPath, this);
    InMemoryFtpDir dir = new InMemoryFtpDir(fsView, this, inMemoryFtpPath.getName());
    name2FtpPaths.put(inMemoryFtpPath.getName(), dir);
    fsView.updatePath(dir);
    fsView.updateListener(dir, FileSystemEvent.EventType.CREATED);
    return dir;
  }

  public InMemoryFtpFile convertToFile(InMemoryFtpPath inMemoryFtpPath) {
    LOG.debug("Convert '{}' in dir '{}' to file.", inMemoryFtpPath, this);
    InMemoryFtpFile file = new InMemoryFtpFile(fsView, this, inMemoryFtpPath.getName());
    name2FtpPaths.put(inMemoryFtpPath.getName(), file);
    fsView.updatePath(file);
    return file;
  }
}
