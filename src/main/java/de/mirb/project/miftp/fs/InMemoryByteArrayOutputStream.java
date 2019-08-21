package de.mirb.project.miftp.fs;

import java.io.ByteArrayOutputStream;

public class InMemoryByteArrayOutputStream extends ByteArrayOutputStream {
  private final InMemoryFtpFile file;
  private boolean closed = false;

  public InMemoryByteArrayOutputStream(InMemoryFtpFile file) {
    this.file = file;
  }

  @Override
  public synchronized void reset() {
    super.reset();
    closed = false;
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() {
    // Closing a <tt>ByteArrayOutputStream</tt> has no effect...
    // super.close();
    if(!closed) {
      closed = true;
      file.setUploadToFinished();
    }
  }
}
