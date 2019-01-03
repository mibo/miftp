package de.mirb.project.miftp.control

import de.mirb.project.miftp.FtpServerConfig
import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.fs.InMemoryFileSystemConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FtpProvider {

  @Value("\${miftp.ftp.user}")
  private var username: String? = null
  @Value("\${miftp.ftp.password}")
  private var password: String? = null
  @Value("\${miftp.ftp.port:50021}")
  var port: Int? = null
  @Value("\${miftp.ftp.maxFiles:0}")
  var maxFiles: Long = 0
  @Value("\${miftp.ftp.ttlInMilliseconds:0}")
  var ttlInMilliseconds: Long = 0
  @Value("\${miftp.ftp.maxMemoryInKilobytes:0}")
  var maxMemoryInKilobytes: Long = 0

  @Bean
  fun server(): MiFtpServer {
    if(username == null || password == null) {
      println("No user and/or password set. Fallback to default ('ftp/ftp')")
      username = "ftp"
      password = "ftp"
    }

    val fsConfig = InMemoryFileSystemConfig.with()
            .maxFiles(maxFiles)
            .maxMemoryInKilobytes(maxMemoryInKilobytes)
            .ttlInMilliseconds(ttlInMilliseconds)
            .create()
    val server = MiFtpServer(FtpServerConfig(port!!, username, password, fsConfig))
    server.startWithSsl()
    println("Started FTP server on port $port (with ssl enabled)")
    return server
  }

  fun getUsername() = username!!
}