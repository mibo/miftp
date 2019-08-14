package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.fs.listener.FileSystemEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpFile extends InMemoryFtpPath {

  private InMemoryByteArrayOutputStream bout;
  private byte[] content;
  private boolean uploadFinished;

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
//      waitForCondition(100, MILLISECONDS, 3, this::isUploadOngoing);
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

  private void waitForCondition(long timeValue, TimeUnit timeUnit, int retries, Supplier<Boolean> condition) {
    while(retries-- > 0 && condition.get()) {
      try {
        Thread.sleep(timeUnit.toMillis(timeValue));
      } catch (InterruptedException e) {
        // should never happen
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public OutputStream createOutputStream(long l) {
    bout = new InMemoryByteArrayOutputStream(this);
    content = null;
    lastModified = System.currentTimeMillis();
    return bout;
  }

  public void uploadFinished() {
    uploadFinished = true;
    fsView.updateListener(this, FileSystemEvent.EventType.CREATED);
  }

  private boolean isUploadOngoing() {
    return !isFlushed();
  }

  public boolean isFlushed() {
    if(bout == null || !uploadFinished) {
      return false;
    } 
    return bout.isClosed();
  }

}
