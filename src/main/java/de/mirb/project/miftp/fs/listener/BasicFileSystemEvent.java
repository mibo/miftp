package de.mirb.project.miftp.fs.listener;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.time.LocalDateTime;

public class BasicFileSystemEvent implements FileSystemEvent {

  private EventType eventType;
  private FtpFile file;
  private User username;
  private final LocalDateTime timestamp = LocalDateTime.now();

//  public BasicFileSystemEvent(EventType eventType, String pathName, String absolutePathName, String username) {
//    this.eventType = eventType;
//    this.pathName = pathName;
//    this.absolutePathName = absolutePathName;
//    this.username = username;
//  }

  public static Builder with(EventType type) {
    return new Builder().eventType(type);
  }

  public static class Builder {
    BasicFileSystemEvent event = new BasicFileSystemEvent();

    private Builder eventType(EventType type) {
      event.eventType = type;
      return this;
    }
    public Builder file(FtpFile file) {
      event.file = file;
      return this;
    }
    public Builder user(User username) {
      event.username = username;
      return this;
    }
    public BasicFileSystemEvent build() {
      return event;
    }
  }

  @Override
  public EventType getType() {
    return eventType;
  }

  @Override
  public FtpFile getFile() {
    return file;
  }

  @Override
  public User getUser() {
    return username;
  }

  @Override
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "BasicFileSystemEvent{" +
        "eventType=" + eventType +
        ", timestamp=" + timestamp +
        ", file=" + file +
        ", username='" + username + '\'' +
        '}';
  }
}
