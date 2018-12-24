package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FtpHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Router {

  @Autowired
  lateinit var handler: FtpHandler

  @Bean
  fun route() = router {

    GET("/files") { ServerResponse.ok().body(fromObject(handler.listFiles("username"))) }
  }
}