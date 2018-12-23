package de.mirb.project.miftp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Router {
  @Bean
  fun route() = router {
    GET("/route") { ServerResponse.ok().body(fromObject(arrayOf(1, 2, 3))) }
  }
}