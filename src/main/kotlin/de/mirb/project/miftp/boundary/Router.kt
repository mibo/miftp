package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.control.FtpHandler
import de.mirb.project.miftp.control.FtpProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.RouterFunctionDsl
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

    //
    GET("/health") {
      return@GET ok().body(fromObject(HealthState("OK", handler.getFilesCount(user))))
    }
    // latest file
    GET("/go/latestFile") {
      val content = it.queryParam("content").isPresent
      return@GET handler.latestFile(user)
              .map { file -> if(content) fileContent(file) else fileData(file) }
              .orElseGet { notFound().build() }
//      ServerResponse.ok().body(fromObject(handler.latestFile(user)))
    }
    // all files
    GET("/files") { ok().body(fromObject(handler.listFiles(user))) }
    // files by id
    GET("/files/{*id}") {
      val content = it.queryParam("content").isPresent
      val id = it.pathVariable("id")
      val fileById = fileView(user, id)

      return@GET fileById.map { file -> if(content) fileContent(file) else fileData(file) }
              .orElseGet { notFound().build() }
    }
    // file content by file id
    GET("/files/{id}/content") {
      val id = it.pathVariable("id")
      val fileById = fileView(user, id)
      return@GET fileById.map { file -> fileContent(file) }
                          .orElseGet { notFound().build() }
    }
  }

  private fun RouterFunctionDsl.fileContent(file: FileEndpoint) =
          ok().contentType(file.contentType()).body(fromObject(file.content()))

  private fun RouterFunctionDsl.fileData(file: FileEndpoint) = ok().body(fromObject(file))

  private fun fileView(user: String, path: String): Optional<FileEndpoint> {
    return handler.getFileByPath(user, path)
  }

  data class HealthState(val state: String, val filesCount: Int)
}