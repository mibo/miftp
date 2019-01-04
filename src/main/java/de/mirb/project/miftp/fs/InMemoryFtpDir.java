package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpDir extends InMemoryFtpPath {

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

  public void runValidation() {
    validate();
    // recurse
    // TODO: only make sense if directories are supported (instead of `InMemoryFtpFile` then `InMemoryFtpPath`)
    name2File.values().forEach(InMemoryFtpFile::runValidation);
  }

  private void validate() {
    while(config.getMaxFiles() > 0 && name2File.size() > config.getMaxFiles()) {
      // remove oldest
      name2File.remove(getOldestFilesName());
    }
    // check max memory size
    long maxMemoryInBytes = config.getmaxMemoryInBytes();
    if(maxMemoryInBytes > 0 && maxMemoryInBytes > currentMemoryConsumption()) {
//      while(maxMemoryInBytes > currentMemoryConsumption()) {
//        name2File.remove(getOldestFilesName());
//      }
      do {
        name2File.remove(getOldestFilesName());
      } while (maxMemoryInBytes > currentMemoryConsumption());
    }
    // check for old files
    long ttlInMilliseconds = config.getTtlInMilliseconds();
    if(ttlInMilliseconds > 0) {
      removeFilesOlderThen(ttlInMilliseconds);
    }
  }

  private void removeFilesOlderThen(long ttlInMilliseconds) {
    name2File.values().stream()
        .filter(f -> (System.currentTimeMillis() - f.getLastModified()) > ttlInMilliseconds)
        .map(InMemoryFtpPath::getName)
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
        .getCount();
  }

  private String getOldestFilesName() {
    if(name2File.isEmpty()) {
      throw new IllegalStateException();
    }
    return name2File.values().stream()
        .min((first, second) -> (int) ((int) first.getLastModified() - second.getLastModified()))
        .map(InMemoryFtpPath::getName).get();
  }

  @Override
  public List<FtpFile> listFiles() {
    return Collections.unmodifiableList(new ArrayList<>(name2File.values()));
  }
}
