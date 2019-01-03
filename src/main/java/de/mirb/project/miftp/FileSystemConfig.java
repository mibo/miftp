package de.mirb.project.miftp;

import org.apache.ftpserver.ftplet.FileSystemFactory;

public interface FileSystemConfig {
  FileSystemFactory createFileSystemFactory();
}
