package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
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

  @Bean
  fun server(): MiFtpServer {
    if(username == null || password == null) {
      println("No user and/or password set. Fallback to default ('ftp/ftp')")
      username = "ftp"
      password = "ftp"
    }

    val server = MiFtpServer(port!!, username, password)
    server.startWithSsl()
    println("Started FTP server on port $port (with ssl enabled)")
    return server
  }

  fun getUsername() = username!!
}