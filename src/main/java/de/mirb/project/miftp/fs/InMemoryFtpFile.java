package de.mirb.project.miftp.fs;

import org.apache.ftpserver.ftplet.User;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpFile extends InMemoryFtpPath {

  private InMemoryByteArrayOutputStream bout;
  private byte[] content;

  public InMemoryFtpFile(InMemoryFsView view, InMemoryFtpDir parentDir, String name) {
    super(view, parentDir, name);
  }

  @Override
  public void cleanUpPath() {
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isFile() {
    return true;
  }

  @Override
  public boolean isRemovable() {
    return isFlushed();
  }

  @Override
  public long getSize() {
    return getContent().length;
  }

  @Override
  public boolean doesExist() {
    return true;
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

  @Override
  public OutputStream createOutputStream(long l) {
    bout = new InMemoryByteArrayOutputStream();
    content = null;
    lastModified = System.currentTimeMillis();
    return bout;
  }


  public boolean isFlushed() {
    if(bout == null || bout.isClosed()) {
      return true;
    }
    return false;
  }

}
