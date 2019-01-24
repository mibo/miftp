package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpPath implements FtpFile {

  protected final InMemoryFtpDir parentDir;
  protected final InMemoryFsView fsView;

  protected final String name;
  protected final User user;
  protected long lastModified;

  public InMemoryFtpPath(InMemoryFsView view, InMemoryFtpDir parentDir, String name) {
    this.fsView = view;
    this.parentDir = parentDir;
    this.name = name;
    this.user = view.getUser();
  }

  @Override
  public String getAbsolutePath() {
    if(parentDir == null) {
      return name;
    }
    String absolutePath = parentDir.getAbsolutePath();
    if(absolutePath.endsWith("/")) {
      absolutePath += name;
    } else {
      absolutePath += "/" + name;
    }
//    if(isDirectory()) {
//      absolutePath += "/";
//    }
    return absolutePath;
  }

  public void cleanUpPath() {
    throw new IllegalStateException("Not supported on a Path instance.");
  }

  public boolean isFlushed() {
    throw new IllegalStateException("Not supported on a Path instance.");
  }

  public InMemoryFtpDir asDir() {
    throw new IllegalStateException("Not a directory.");
  }

  public InMemoryFtpFile asFile() {
    throw new IllegalStateException("Not a file.");
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean doesExist() {
    return false;
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
    throw new IllegalStateException("Not supported on a Path instance.");
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
    return 0;
//    throw new IllegalStateException("Not supported on a Path instance.");
  }

  @Override
  public Object getPhysicalFile() {
    return null;
  }

  @Override
  public boolean mkdir() {
    parentDir.convertToDir(this);
    return true;
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
  public List<InMemoryFtpPath> listFiles() {
    return Collections.emptyList();
  }

  @Override
  public OutputStream createOutputStream(long offset) throws IOException {
    InMemoryFtpFile file = parentDir.convertToFile(this);
    return file.createOutputStream(offset);
  }

  @Override
  public InputStream createInputStream(long offset) throws IOException {
    throw new IllegalStateException("Not supported on a Path instance.");
  }

  @Override
  public String toString() {
    String type = isFile()? "File": isDirectory()? "Dir": "<path>";
    return type + " {parentDir='" +
        (parentDir == null ? "<root> ": parentDir.getAbsolutePath()) +
        "', name='" + name + "'}";
  }
}
