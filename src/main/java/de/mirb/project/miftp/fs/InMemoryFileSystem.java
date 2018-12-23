package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFileSystem implements FileSystemFactory {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryFileSystem.class);

  private final static Map<String, FileSystemView> user2View = new ConcurrentHashMap<>();

  @Override
  public FileSystemView createFileSystemView(User user) throws FtpException {
//    LOG.info("Known user2views -> {}", user2View.toString());
    return user2View.computeIfAbsent(user.getName(), (u) -> new InMemoryFsView(user));
  }
}
