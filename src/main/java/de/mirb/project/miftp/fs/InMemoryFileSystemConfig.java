package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.FileSystemConfig;
import org.apache.ftpserver.ftplet.FileSystemFactory;

public class InMemoryFileSystemConfig implements FileSystemConfig {
  private long ttlInMilliseconds = 0;
  private long maxMemoryInKilobytes = 0;
  private long maxFiles = 0;

  public static class Builder {
    InMemoryFileSystemConfig config = new InMemoryFileSystemConfig();

    public Builder ttlInMilliseconds(long ttlInMilliseconds) {
      config.ttlInMilliseconds = ttlInMilliseconds;
      return this;
    }
    public Builder maxMemoryInKilobytes(long maxMemoryInKilobytes) {
      config.maxMemoryInKilobytes = maxMemoryInKilobytes;
      return this;
    }
    public Builder maxFiles(long maxFiles) {
      config.maxFiles = maxFiles;
      return this;
    }

    public InMemoryFileSystemConfig create() {
      return config;
    }
  }

  public static Builder with() {
    return new Builder();
  }

  public long getTtlInMilliseconds() {
    return ttlInMilliseconds;
  }


  public long getMaxMemoryInKilobytes() {
    return maxMemoryInKilobytes;
  }

  public long getMaxFiles() {
    return maxFiles;
  }

  @Override
  public InMemoryFileSystem createFileSystemFactory() {
    return InMemoryFileSystem.with(this);
  }
}
