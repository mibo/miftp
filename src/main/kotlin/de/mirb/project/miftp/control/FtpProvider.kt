package de.mirb.project.miftp.control

import de.mirb.project.miftp.MiFtpServer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FtpProvider {
//  @Bean
//  fun server() = MiFtpServer(50021, "username", "password")

  @Bean
  fun server(): MiFtpServer {
    var server = MiFtpServer(50021, "username", "password")
    server.startWithSsl()
    return server
  }
}