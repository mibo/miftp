package de.mirb.project.miftp.boundary

import org.apache.ftpserver.ftplet.FtpFile

data class FileView(val name: String, val lastModified: Long, val size: Long) {
//  val name = ftpFile.name
//  val lastModified = ftpFile.lastModified
//  val size = ftpFile.size

  companion object Factory {
    fun create(ftpFile: FtpFile) = FileView(ftpFile.name, ftpFile.lastModified, ftpFile.size)
  }
}
