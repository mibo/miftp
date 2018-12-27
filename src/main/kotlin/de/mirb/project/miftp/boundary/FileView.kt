package de.mirb.project.miftp.boundary

import org.apache.ftpserver.ftplet.FtpFile
import org.springframework.http.MediaType
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

data class FileView(val name: String, val lastModified: Long, val size: Long, private val ftpFile: FtpFile) {
//  val name = ftpFile.name
//  val lastModified = ftpFile.lastModified
//  val size = ftpFile.size

//  val ftpFile: ftpFile

  companion object Factory {
    fun create(ftpFile: FtpFile) = FileView(ftpFile.name, ftpFile.lastModified, ftpFile.size, ftpFile)
  }

  fun isFile() = ftpFile.isFile

  fun content(): ByteBuffer {
    if(ftpFile.isFile) {
      val ins = ftpFile.createInputStream(0)
      //val ba = ByteArray()
      return ByteBuffer.wrap(ins.buffered().use { it.readAllBytes() })
    }
    // TODO: check if return empty content is better
    throw IllegalArgumentException("Unable to get content from directory")
  }

  fun contentType(): MediaType {
    if(name.toLowerCase().endsWith("jpg") || name.toLowerCase().endsWith("jpeg")) {
      return MediaType.IMAGE_JPEG
    }
    if(name.toLowerCase().endsWith("png")) {
      return MediaType.IMAGE_PNG
    }
    return MediaType.APPLICATION_OCTET_STREAM
  }
}
