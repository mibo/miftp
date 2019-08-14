package de.mirb.project.miftp.fs;

import java.io.ByteArrayOutputStream;

public class InMemoryByteArrayOutputStream extends ByteArrayOutputStream {
  private final InMemoryFtpFile file;
  private boolean closed = false;

  public InMemoryByteArrayOutputStream(InMemoryFtpFile file) {
    this.file = file;
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() {
    closed = true;
    file.uploadFinished();
  }
}
