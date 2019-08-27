package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.FileSystemConfig;
import de.mirb.project.miftp.fs.listener.FileSystemListener;

public class InMemoryFileSystemConfig implements FileSystemConfig {
  private long ttlInMilliseconds = 0;
  private long maxMemoryInBytes = 0;
  private long maxFiles = 0;
  private int cleanupInterval = 0;
  private boolean removeEmptyDirs = false;
  /** default is an empty implementation */
  private FileSystemListener fileSystemListener = event -> {  };

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
    public Builder cleanUpInterval(int cleanupInterval) {
      config.cleanupInterval = cleanupInterval;
      return this;
    }
    public Builder removeEmptyDirs(boolean removeEmptyDirs) {
      config.removeEmptyDirs = removeEmptyDirs;
      return this;
    }
    public Builder fileSystemListener(FileSystemListener listener) {
      config.fileSystemListener = listener;
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

  public boolean isRemoveEmptyDirs() {
    return removeEmptyDirs;
  }

  public long getMaxMemoryInBytes() {
    return maxMemoryInBytes;
  }

  public long getMaxFiles() {
    return maxFiles;
  }

  public int getCleanupInterval() {
    return cleanupInterval;
  }

  public FileSystemListener getFileSystemListener() {
    return fileSystemListener;
  }

  @Override
  public InMemoryFileSystem createFileSystemFactory() {
    return InMemoryFileSystem.with(this);
  }

  @Override
  public String toString() {
    return "InMemoryFileSystemConfig{" +
        "ttlInMilliseconds=" + ttlInMilliseconds +
        ", maxMemoryInBytes=" + maxMemoryInBytes +
        ", maxFiles=" + maxFiles +
        ", removeEmptyDirs=" + removeEmptyDirs +
        ", cleanupInterval=" + cleanupInterval +
        '}';
  }
}
