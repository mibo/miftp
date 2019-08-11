package de.mirb.project.miftp.fs.listener;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.time.LocalDateTime;

public interface FileSystemEvent {
  enum EventType { CREATED, MODIFIED, DELETED }

  LocalDateTime getTimestamp();
  EventType getType();
  FtpFile getFile();
  User getUser();
}
