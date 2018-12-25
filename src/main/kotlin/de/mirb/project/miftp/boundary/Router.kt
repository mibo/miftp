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

    val user = "username"
    GET("/files") { ServerResponse.ok().body(fromObject(handler.listFiles(user))) }
    GET("/files/{id}") {
      val id = it.pathVariable("id")
      val fileContentById = handler.getFileContentById(user, id)
      if(fileContentById == null) {
        notFound().build()
      } else {
        ServerResponse.ok().body(fromObject(fileContentById))
      }
    }
  }
}