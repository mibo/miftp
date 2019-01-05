package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.FileSystemConfig;

public class InMemoryFileSystemConfig implements FileSystemConfig {
  private long ttlInMilliseconds = 0;
  private long maxMemoryInBytes = 0;
  private long maxFiles = 0;

  public static class Builder {
    InMemoryFileSystemConfig config = new InMemoryFileSystemConfig();

    public Builder ttlInMilliseconds(long ttlInMilliseconds) {
      config.ttlInMilliseconds = ttlInMilliseconds;
      return this;
    }
    public Builder maxMemoryInBytes(long maxMemoryInBytes) {
      config.maxMemoryInBytes = maxMemoryInBytes;
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


  public long getMaxMemoryInBytes() {
    return maxMemoryInBytes;
  }

  public long getMaxFiles() {
    return maxFiles;
  }

  @Override
  public InMemoryFileSystem createFileSystemFactory() {
    return InMemoryFileSystem.with(this);
  }
}
