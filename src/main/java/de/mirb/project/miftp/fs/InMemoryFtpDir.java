package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpDir extends InMemoryFtpPath {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFtpDir.class);

  private final Map<String, InMemoryFtpFile> name2File = new HashMap<>();
  private final InMemoryFileSystemConfig config;

  public InMemoryFtpDir(String name, User user, InMemoryFileSystemConfig config) {
    this(null, name, user, config);
  }

  public InMemoryFtpDir(InMemoryFtpDir parentDir, String name, User user, InMemoryFileSystemConfig config) {
    super(parentDir, name, user);
    this.config = config;
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

  /**
   * Remove all 'stale' (based on config) files in this directory and
   * invoke this method on all remaining paths (dir and file).
   */
  public void cleanUpPath() {
    LOG.debug("Run cleanup path.");
    cleanUpFiles();
    // recurse
    // TODO: only make sense if directories are supported (instead of `InMemoryFtpFile` then `InMemoryFtpPath`)
    name2File.values().forEach(InMemoryFtpFile::cleanUpPath);
  }

  private void cleanUpFiles() {
    while(config.getMaxFiles() > 0 && name2File.size() > config.getMaxFiles()) {
      LOG.debug("Run cleanup path for max files '{}' with current '{}' files listed.",
          config.getMaxFiles(), name2File.size());
      // remove oldest
      name2File.remove(getOldestFilesName());
    }
    // check max memory size
    long maxMemoryInBytes = config.getMaxMemoryInBytes();
    if(maxMemoryInBytes > 0 && maxMemoryInBytes > currentMemoryConsumption()) {
      LOG.debug("Run cleanup path for maxMemoryInBytes '{}'.", maxMemoryInBytes);
//      while(maxMemoryInBytes > currentMemoryConsumption()) {
//        name2File.remove(getOldestFilesName());
//      }
      do {
        InMemoryFtpFile removed = name2File.remove(getOldestFilesName());
        LOG.debug("Removed '{}' for bytes '{}'.", removed.getName(), removed.getSize());
      } while (maxMemoryInBytes > currentMemoryConsumption());
    }
    // check for old files
    long ttlInMilliseconds = config.getTtlInMilliseconds();
    if(ttlInMilliseconds > 0) {
      LOG.debug("Run cleanup path for ttlInMilliseconds '{}'.", ttlInMilliseconds);
      removeFilesOlderThen(ttlInMilliseconds);
    }
  }

  private void removeFilesOlderThen(long ttlInMilliseconds) {
    name2File.values().stream()
        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
        .filter(InMemoryFtpPath::isRemovable)
        .peek((name) -> LOG.debug("Remove '{}' with timestamp '{}'", name.getName(), name.getLastModified()))
        .map(InMemoryFtpPath::getName)
//        .peek((name) -> LOG.debug("Remove {}", name))
        .forEach(name2File::remove);
//        .forEach(f -> name2File.remove(f.getName()));

//    List<InMemoryFtpFile> inMemoryFtpFileStream = name2File.values().stream()
//        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
//        .collect(Collectors.toList());
//    inMemoryFtpFileStream.forEach(file -> name2File.remove(file.getName()));
  }


  //  private void removeFilesOlderThen(long ttlInMilliseconds) {
//    boolean run = true;
//    while(!name2File.isEmpty() && run) {
//      InMemoryFtpFile oldestFile = name2File.get(getOldestFilesName());
//      long age = System.currentTimeMillis() - oldestFile.getLastModified();
//      if(age > ttlInMilliseconds) {
//        name2File.remove(oldestFile.getName());
//      } else {
//        run = false;
//      }
//    }
//  }

  private long currentMemoryConsumption() {
    if(name2File.isEmpty()) {
      return 0;
    }
    return name2File.values().stream()
        .collect(Collectors.summarizingLong(InMemoryFtpFile::getSize))
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
        .filter(InMemoryFtpFile::isFile)
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
  public List<FtpFile> listFiles() {
    return Collections.unmodifiableList(new ArrayList<>(name2File.values()));
  }
}
