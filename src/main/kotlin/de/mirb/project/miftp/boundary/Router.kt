package de.mirb.project.miftp.boundary

import de.mirb.project.miftp.config.BeanProvider
import de.mirb.project.miftp.config.BuildInfo
import de.mirb.project.miftp.control.FileAccessHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.RouterFunctionDsl
import org.springframework.web.reactive.function.server.router
import java.util.*

@Configuration
class Router {

  @Autowired
  lateinit var handler: FileAccessHandler
  @Autowired
  lateinit var beanProvider: BeanProvider
  @Autowired
  lateinit var buildInfo: BuildInfo

  @Bean
  fun route() = router {
    val user = beanProvider.getUsername()

    // basic health state
    GET("/health") {
      return@GET ok().body(fromValue(HealthState("OK", handler.getFilesCount(user), buildInfo)))
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
    GET("/files") { ok().body(fromValue(handler.listFiles(user))) }
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
    // latest file
    GET("/go/token/{token}") {
      val content = it.queryParam("content").isPresent
      val token = it.pathVariable("token")
      return@GET handler.getFileByToken(token)
              .map { file -> if(content) fileContent(file) else fileData(file) }
              .orElseGet { notFound().build() }
    }
  }

  private fun RouterFunctionDsl.fileContent(file: FileEndpoint) =
          ok().contentType(file.contentType()).body(fromValue(file.content()))

  private fun RouterFunctionDsl.fileData(file: FileEndpoint) = ok().body(fromValue(file))

  private fun fileView(user: String, path: String): Optional<FileEndpoint> {
    return handler.getFileByPath(user, path)
  }

  data class HealthState(val state: String, val filesCount: Int, val buildInfo: BuildInfo)
}