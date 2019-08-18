package de.mirb.project.miftp.control

import de.mirb.project.miftp.FtpServerConfig
import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.control.notifier.SlackImageDiffNotifier
import de.mirb.project.miftp.control.notifier.SlackNotifier
import de.mirb.project.miftp.fs.InMemoryFileSystemConfig
import de.mirb.project.miftp.fs.listener.FileSystemListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FtpProvider {

  @Value("\${miftp.ftp.user}")
  private var username: String? = null
  @Value("\${miftp.ftp.password}")
  private var password: String? = null
  @Value("\${miftp.ftp.port:50021}")
  var port: Int? = null
  @Value("\${miftp.ftp.pasvPorts:50100-50200}")
  var pasvPorts: String? = null
  @Value("\${miftp.ftp.pasvAddress:}")
  var pasvAddress: String? = ""
  @Value("\${miftp.ftp.pasvExtAddress:}")
  var pasvExtAddress: String? = ""
  @Value("\${miftp.ftp.maxFiles:0}")
  var maxFiles: Long = 0
  @Value("\${miftp.ftp.ttlInMilliseconds:0}")
  var ttlInMilliseconds: Long = 0
  @Value("\${miftp.ftp.maxMemoryInBytes:0}")
  var maxMemoryInBytes: Long = 0
  @Value("\${miftp.ftp.cleanupInterval}")
  var cleanupInterval: Int = 10
  @Value("\${miftp.keystore.name:}")
  var keystoreName: String? = ""
  @Value("\${miftp.keystore.password:}")
  var keystorePassword: String? = ""
  @Value("\${miftp.eventListener:}")
  var eventListener: String? = ""
  @Value("\${miftp.eventListener.failOnMissing:}")
  var failOnMissingEventListener: Boolean = false
  @Value("#{\${miftp.eventListener.parameters}}")
  var eventListenerParameters: Map<String, String> = HashMap()

//  var test: RestClientAutoConfiguration
  var fileCreatedNotifier = FileSystemListener {
    println("${it.user.name} has ${it.type.name} the path ${it.file.absolutePath} at ${it.timestamp}")
  }

  @Bean
  fun server(): MiFtpServer {
    if(username == null || password == null) {
      println("No user and/or password set. Fallback to default ('ftp/ftp')")
      username = "ftp"
      password = "ftp"
    }

    val fsConfig = InMemoryFileSystemConfig.with()
            .maxFiles(maxFiles)
            .maxMemoryInBytes(maxMemoryInBytes)
            .ttlInMilliseconds(ttlInMilliseconds)
            .cleanUpInterval(cleanupInterval)
            .fileSystemListener(getFtpEventListener())
            .create()

    val keystoreFile =
      if (keystoreName == null) keystoreName
      else keystoreName!!.removePrefix("classpath:")
    val serverConfig = FtpServerConfig.with(port!!)
            .username(username)
            .password(password)
            .pasvPorts(pasvPorts)
            .pasvAddress(pasvAddress)
            .pasvExtAddress(pasvExtAddress)
            .fileSystemConfig(fsConfig)
            .keystoreName(keystoreFile)
            .keystorePassword(keystorePassword)
            .build()
    val server = MiFtpServer(serverConfig)
    server.start()
    println("Started FTP server on port $port (with ssl enabled) and config $serverConfig")
    return server
  }

  fun getUsername() = username!!

  private fun getFtpEventListener(): FileSystemListener {
    return when (eventListener) {
      "" -> FileSystemListener { }
      "SlackNotifier" -> SlackNotifier().init(eventListenerParameters)
      "SlackImageDiffNotifier" -> SlackImageDiffNotifier().init(eventListenerParameters)
      else -> handleMissingEventListener()
    }
  }

  private fun handleMissingEventListener(): FileSystemListener {
    if(failOnMissingEventListener) {
      throw IllegalStateException("ERROR: Configured event listener $eventListener is not available.")
    }
    return FileSystemListener {
      println("WARNING: Configured event listener $eventListener is not available. (Received an event at ${it.timestamp})")
    }
  }
}