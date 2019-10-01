package de.mirb.project.miftp.control

import org.apache.ftpserver.ftplet.FtpFile
import java.util.*
import kotlin.collections.HashMap

class FtpFileStore(private val maxFileCount: Int) {

  private val token2File = HashMap<String, FtpFile>()
  private val fileTokenQueue = LinkedList<String>() as Queue<String>
  /**
   * Add given file to token based access list and generate access token.
   */
  fun enableTokenBasedAccess(file: FtpFile): String {
    if (fileTokenQueue.size == maxFileCount) {
      token2File.remove(fileTokenQueue.poll())
    }
    val token = UUID.randomUUID().toString()
    fileTokenQueue.add(token)
    token2File[token] = file
    return token
  }

  fun getFileCount() = fileTokenQueue.size

  fun getFileByToken(token: String) = Optional.ofNullable(token2File[token])

  fun removeTokenBasedAccess(token: String) {
    fileTokenQueue.remove(token)
    token2File.remove(token)
  }
}