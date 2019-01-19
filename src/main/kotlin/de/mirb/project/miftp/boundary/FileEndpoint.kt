package de.mirb.project.miftp.boundary

import org.apache.ftpserver.ftplet.FtpFile
import org.springframework.http.MediaType
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.text.DateFormatter

data class FileEndpoint(val name: String, val lastModified: Long, val size: Long, private val ftpFile: FtpFile) {
//  val name = ftpFile.name
//  val lastModified = ftpFile.lastModified
//  val size = ftpFile.size

//  val ftpFile: ftpFile

  val df = SimpleDateFormat("HH:mm, EEE, MMM d, ''yy")
  val lastModifiedFormatted = df.format(Date(lastModified))

  companion object Factory {
    fun create(ftpFile: FtpFile) = FileEndpoint(ftpFile.name, ftpFile.lastModified, ftpFile.size, ftpFile)
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
    val lcName = name.toLowerCase()
    if(lcName.endsWith("jpg") || lcName.endsWith("jpeg")) {
      return MediaType.IMAGE_JPEG
    }
    if(lcName.endsWith("png")) {
      return MediaType.IMAGE_PNG
    }
    return MediaType.APPLICATION_OCTET_STREAM
  }
}
