package de.mirb.project.miftp

import de.mirb.project.miftp.format.SizeFormatter
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@SpringBootApplication
class MiftpApplication {

  @Bean("sizeFormatter")
  fun sizeFormatter() = SizeFormatter()

  @Bean("buildInfo")
  fun buildInfo() = buildInfo
  var buildInfo: BuildInfo = BuildInfo("<unset>", "<unset>")

  @Bean
  @Profile("!test")
  fun init(context: ApplicationContext) = CommandLineRunner {

    try {
      val buildProperties = context.getBean(BuildProperties::class.java)
      handleInfo(buildProperties)
    } catch (e: Exception) {
      // just ignore?
      // workaround for https://youtrack.jetbrains.com/issue/IDEA-201587
      println("got exception <" + e.message + ">")
    }
  }

  private fun handleInfo(buildProperties: BuildProperties) {
    println("build version is <" + buildProperties.version + ">")
    println("build time is <" + buildProperties.time + ">")

//    buildInfo = BuildInfo(buildProperties.version, buildProperties.time.toString())
    buildInfo.version = buildProperties.version
    buildInfo.timestamp = buildProperties.time.toString()
//    println("value for custom key 'foo' is <" + buildProperties.get("foo") + ">")
  }
}

data class BuildInfo(var version: String, var timestamp: String)

fun main(args: Array<String>) {
  runApplication<MiftpApplication>(*args)
}

