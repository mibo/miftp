package de.mirb.project.miftp.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpDir extends InMemoryFtpPath {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFtpDir.class);

  private final Map<String, InMemoryFtpPath> name2File = new HashMap<>();
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
  public boolean doesExist() {
    return true;
  }

  @Override
  public boolean mkdir() {
    // is already a directory
    return false;
  }

  private InMemoryFtpPath createPath(String name) {
    return new InMemoryFtpPath(fsView, this, name);
  }

  public InMemoryFtpPath grantPath(String name) {
    if(LOG.isDebugEnabled()) {
      LOG.debug("Grant path '{}' (exists={}).", name, name2File.containsKey(name));
    }
    return name2File.computeIfAbsent(name, this::createPath);
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
    name2File.values().forEach(InMemoryFtpPath::cleanUpPath);
  }

  private void cleanUpFiles() {
    while(config.getMaxFiles() > 0 && name2File.size() > config.getMaxFiles()) {
      LOG.debug("Run cleanup path for max files '{}' with current '{}' files listed.",
          config.getMaxFiles(), name2File.size());
      // remove oldest
      removeFile(getOldestFilesName());
    }
    // check max memory size
    long maxMemoryInBytes = config.getMaxMemoryInBytes();
    if(maxMemoryInBytes > 0) {
      long currentMemoryConsumption = currentMemoryConsumption();
      LOG.debug("Run cleanup path for maxMemoryInBytes '{}' with current consumption '{}'.",
          maxMemoryInBytes, currentMemoryConsumption);

      while (currentMemoryConsumption > maxMemoryInBytes) {
        InMemoryFtpPath removed = removeFile(getOldestFilesName());
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

  private InMemoryFtpPath removeFile(String name) {
    InMemoryFtpPath removed = name2File.remove(name);
    fsView.removePath(removed);
    return removed;
  }

  private void removeFilesOlderThen(long ttlInMilliseconds) {
    List<InMemoryFtpPath> toRemove = name2File.values().stream()
        .filter(InMemoryFtpPath::isFile)
        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
        .filter(InMemoryFtpPath::isRemovable)
        .peek((name) -> LOG.debug("Remove '{}' with timestamp '{}'", name.getName(), name.getLastModified()))
//        .peek((name) -> LOG.debug("Remove {}", name))
        .collect(Collectors.toList());

    toRemove.forEach(f -> removeFile(f.getName()));
  }

  private long currentMemoryConsumption() {
    if(name2File.isEmpty()) {
      return 0;
    }
    return name2File.values().stream()
        .collect(Collectors.summarizingLong(InMemoryFtpPath::getSize))
        .getSum();
  }

  /**
   * Oldest filename which is remove able (only for files directories are skipped).
   *
   * @return
   */
  private String getOldestFilesName() {
    if(name2File.isEmpty()) {
      throw new IllegalStateException();
    }
    return name2File.values().stream()
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
    return Collections.unmodifiableList(new ArrayList<>(name2File.values()));
  }

  public InMemoryFtpDir convertToDir(InMemoryFtpPath inMemoryFtpPath) {
    LOG.debug("Convert '{}' in dir '{}' to directory.", inMemoryFtpPath, this);
    InMemoryFtpDir dir = new InMemoryFtpDir(fsView, this, inMemoryFtpPath.getName());
    name2File.put(inMemoryFtpPath.getName(), dir);
    fsView.updatePath(dir);
    return dir;
  }

  public InMemoryFtpFile convertToFile(InMemoryFtpPath inMemoryFtpPath) {
    LOG.debug("Convert '{}' in dir '{}' to file.", inMemoryFtpPath, this);
    InMemoryFtpFile file = new InMemoryFtpFile(fsView, this, inMemoryFtpPath.getName());
    name2File.put(inMemoryFtpPath.getName(), file);
    fsView.updatePath(file);
    return file;
  }
}
