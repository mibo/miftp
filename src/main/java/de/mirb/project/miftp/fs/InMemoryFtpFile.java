package de.mirb.project.miftp.fs;

import de.mirb.project.miftp.fs.listener.FileSystemEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by mibo on 21.04.17.
 */
public class InMemoryFtpFile extends InMemoryFtpPath {

  private InMemoryByteArrayOutputStream bout = new InMemoryByteArrayOutputStream(this);
  private byte[] content;
  private boolean uploadFinished;
  private boolean locked;

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
    return !isLocked();
  }

  @Override
  public boolean delete() {
    if(isRemovable()) {
      return forceDelete();
    }
    return false;
  }

  @Override
  public boolean forceDelete() {
    bout.reset();
//    content = null;

    fsView.removePath(this);
    parentDir.removeChildPath(this);
    return true;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isLocked() {
    return locked;
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
    return getContent(true);
  }

  private byte[] getContent(boolean wait) {
    if(content != null) {
      return content;
    }

    synchronized (name) {
      if(isFlushed()) {
        content = bout.toByteArray();
        return content;
      } else if(wait) {
        waitForCondition(100, MILLISECONDS, 3, this::isUploadOngoing);
        return getContent(false);
      }
      return new byte[0];
    }
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
//    System.out.println(String.format("LOG::createOutputStream(%d) called for: %s", l, getName()));
    bout.reset();
    content = null;
    lastModified = System.currentTimeMillis();
    return bout;
  }

  /**
   * If called the file is handled as completely uploaded.
   * This is only called from the {@link InMemoryByteArrayOutputStream}.
   */
  void setUploadToFinished() {
    if(!uploadFinished) {
      uploadFinished = true;
      // ..
      getContent(false);
      fsView.updateListener(this, FileSystemEvent.EventType.CREATED);
    }
  }

  private boolean isUploadOngoing() {
    return !isFlushed();
  }

  /**
   * Flushed is true if an upload finished (but false if nothing was uploaded yet)
   * @return
   */
  public boolean isFlushed() {
    if(uploadFinished) {
      return true;
    } else if(bout != null) {
      return bout.isClosed();
    }
    return false;
  }
}
