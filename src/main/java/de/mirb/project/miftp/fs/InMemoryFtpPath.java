package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by mibo on 21.04.17.
 */
public abstract class InMemoryFtpPath implements FtpFile {

  protected final InMemoryFtpDir parentDir;
  protected final String name;
  protected final User user;

  private long lastModified;
  private InMemoryByteArrayOutputStream bout;
  private byte[] content;


  public InMemoryFtpPath(InMemoryFtpDir parentDir, String name, User user) {
    if(parentDir != null) {
      if(name.equals("/")) {
        parentDir = null;
      }
    }
    this.parentDir = parentDir;
    this.name = name;
    this.user = user;
  }

  @Override
  public String getAbsolutePath() {
    if(parentDir == null) {
      return name;
    }
    return parentDir.getAbsolutePath() + name;
  }

  public abstract void cleanUpPath();

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public boolean doesExist() {
    return true;
  }

  @Override
  public boolean isReadable() {
    return true;
  }

  @Override
  public boolean isWritable() {
//    return isDirectory();
    return true;
  }

  @Override
  public boolean isRemovable() {
    return isFlushed();
  }

  @Override
  public String getOwnerName() {
    return user.getName();
  }

  @Override
  public String getGroupName() {
    return null;
  }

  @Override
  public int getLinkCount() {
    return 0;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public boolean setLastModified(long l) {
    lastModified = l;
    return true;
  }

  @Override
  public long getSize() {
    return getContent().length;
  }

  @Override
  public Object getPhysicalFile() {
    return null;
  }

  @Override
  public boolean mkdir() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  @Override
  public boolean move(FtpFile ftpFile) {
    return false;
  }

  @Override
  public List<FtpFile> listFiles() {
    return Collections.emptyList();
  }

  @Override
  public OutputStream createOutputStream(long l) {
    bout = new InMemoryByteArrayOutputStream();
    content = null;
    lastModified = System.currentTimeMillis();
    return bout;
  }

  @Override
  public InputStream createInputStream(long l) {
    return new ByteArrayInputStream(getContent());
  }

  private byte[] getContent() {
    synchronized (name) {
      if(isFlushed()) {
        if(content == null) {
          content = bout.toByteArray();
        }
      } else {
        return new byte[0];
      }
    }
    return content;
  }

  public boolean isFlushed() {
    if(bout == null || bout.isClosed()) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Path {parentDir='" +
        (parentDir == null ? "<root> ": parentDir.getAbsolutePath()) +
        "', name='" + name + "'}";
  }
}
