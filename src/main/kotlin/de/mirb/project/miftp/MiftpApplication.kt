package de.mirb.project.miftp

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class MiftpApplication {

  @Bean
  fun init(context: ApplicationContext) = CommandLineRunner {

    try {
      val buildProperties = context.getBean(BuildProperties::class.java)
      displayInfo(buildProperties)
    } catch (e: Exception) {
      // just ignore?
      // workaround for https://youtrack.jetbrains.com/issue/IDEA-201587
      println("got exception <" + e.message + ">")
    }
  }

  private fun displayInfo(buildProperties: BuildProperties) {
    println("build version is <" + buildProperties.version + ">")
    println("build time is <" + buildProperties.time + ">")
//    println("value for custom key 'foo' is <" + buildProperties.get("foo") + ">")
  }
}

fun main(args: Array<String>) {
  runApplication<MiftpApplication>(*args)
}

