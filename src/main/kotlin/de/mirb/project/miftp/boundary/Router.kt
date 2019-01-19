package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FtpHandler
import de.mirb.project.miftp.control.FtpProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.util.*

@Configuration
class Router {

  @Autowired
  lateinit var handler: FtpHandler
  @Autowired
  lateinit var ftpProvider: FtpProvider

  @Bean
  fun route() = router {
    val user = ftpProvider.getUsername()

    GET("/files") { ServerResponse.ok().body(fromObject(handler.listFiles(user))) }
    GET("/files/{id}") {
      val id = it.pathVariable("id")
      val fileById = fileView(user, id)
      return@GET fileById.map { file -> ok().body(fromObject(file)) }
                          .orElseGet { notFound().build() }
    }
    GET("/files/{id}/content") {
      val id = it.pathVariable("id")
      val fileById = fileView(user, id)
      return@GET fileById.map { file -> ok().contentType(file.contentType()).body(fromObject(file.content())) }
                          .orElseGet { notFound().build() }
    }
  }

  private fun fileView(user: String, id: String): Optional<FileEndpoint> {
    return handler.getFileById(user, id)
  }
}