package de.mirb.project.miftp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MiftpApplication

fun main(args: Array<String>) {
  runApplication<MiftpApplication>(*args)

  val server = MiFtpServer(50021, "username", "password")
//  server.startWithPlain()
  server.startWithSsl()
}

