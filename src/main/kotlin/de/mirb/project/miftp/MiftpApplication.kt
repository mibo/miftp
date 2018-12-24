package de.mirb.project.miftp

import de.mirb.project.miftp.control.FtpHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MiftpApplication

@Autowired
lateinit var handler: FtpHandler

fun main(args: Array<String>) {
  runApplication<MiftpApplication>(*args)
}

