package de.mirb.project.miftp.fs;

import java.io.ByteArrayOutputStream;

public class InMemoryByteArrayOutputStream extends ByteArrayOutputStream {
  private boolean closed = false;

  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() {
//    super.close();
    closed = true;
  }
}
