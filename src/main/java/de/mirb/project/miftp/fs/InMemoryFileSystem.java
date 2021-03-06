package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFileSystem implements FileSystemFactory {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFileSystem.class);

  private final InMemoryFileSystemConfig config;

  public static class Builder {
    private final InMemoryFileSystemConfig.Builder config;

    public Builder(InMemoryFileSystemConfig.Builder config) {
      this.config = config;
    }

    public InMemoryFileSystem create() {
      return config.create().createFileSystemFactory();
    }

    public Builder ttlInMilliseconds(long ttlInMilliseconds) {
      config.ttlInMilliseconds(ttlInMilliseconds);
      return this;
    }
    public Builder maxMemoryInBytes(long maxMemoryInBytes) {
      config.maxMemoryInBytes(maxMemoryInBytes);
      return this;
    }
    public Builder maxFiles(long maxFiles) {
      config.maxFiles(maxFiles);
      return this;
    }
  }

  public static Builder with() {
    return new Builder(InMemoryFileSystemConfig.with());
  }

  public static InMemoryFileSystem with(InMemoryFileSystemConfig config) {
    return new InMemoryFileSystem(config);
  }

  private InMemoryFileSystem(InMemoryFileSystemConfig config) {
    this.config = config;
  }

  @Override
  public FileSystemView createFileSystemView(User user) throws FtpException {
    LOG.info("Request FileSystemView for user {}", user.getName());
//    LOG.debug("Request FileSystemView was {}", (user2View.containsKey(user.getName())? "reused": "created"));
    return InMemoryFsView.createView(user, config);
//    return user2View.computeIfAbsent(user.getName(), (u) -> InMemoryFsView.createView(user, config));
  }
}
