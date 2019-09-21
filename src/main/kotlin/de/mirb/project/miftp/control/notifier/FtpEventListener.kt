package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.control.FtpFileStore
import de.mirb.project.miftp.fs.listener.FileSystemListener

interface FtpEventListener : FileSystemListener {

  fun init(parameters: Map<String, String>, ftpFileStore: FtpFileStore): FtpEventListener
}