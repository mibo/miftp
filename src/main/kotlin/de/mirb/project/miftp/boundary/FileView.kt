package de.mirb.project.miftp.boundary

import org.apache.ftpserver.ftplet.FtpFile

data class FileView(private val ftpFile: FtpFile) {
  val name = ftpFile.name
  val lastModified = ftpFile.lastModified
  val size = ftpFile.size
}