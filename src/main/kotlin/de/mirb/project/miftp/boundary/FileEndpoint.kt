package de.mirb.project.miftp.boundary

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.ftpserver.ftplet.FtpFile
import org.springframework.http.MediaType
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

data class FileEndpoint(val name: String, val absolutePath: String, val lastModified: Long, val size: Long, private val ftpFile: FtpFile) {
//  val name = ftpFile.name
//  val lastModified = ftpFile.lastModified
//  val size = ftpFile.size

//  val ftpFile: ftpFile

  private val df = SimpleDateFormat("HH:mm:ss, EEE, MMM d, ''yy")
  @JsonIgnore val lastModifiedFormatted = df.format(Date(lastModified))

  val lastModifiedDate = Date(lastModified)

  companion object Factory {
    fun create(ftpFile: FtpFile) = FileEndpoint(ftpFile.name, ftpFile.absolutePath, ftpFile.lastModified, ftpFile.size, ftpFile)
  }

  fun isFile() = ftpFile.isFile

  fun content(): ByteBuffer {
    if(ftpFile.isFile) {
      val ins = ftpFile.createInputStream(0)
      //val ba = ByteArray()
      return ByteBuffer.wrap(ins.buffered().use { it.readBytes() })
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
