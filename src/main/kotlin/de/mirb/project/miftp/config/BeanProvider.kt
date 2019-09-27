package de.mirb.project.miftp.config

import de.mirb.project.miftp.FtpServerConfig
import de.mirb.project.miftp.MiFtpServer
import de.mirb.project.miftp.control.FtpFileStore
import de.mirb.project.miftp.control.notifier.SlackImageDiffNotifier
import de.mirb.project.miftp.control.notifier.SlackNotifier
import de.mirb.project.miftp.format.SizeFormatter
import de.mirb.project.miftp.fs.InMemoryFileSystemConfig
import de.mirb.project.miftp.fs.listener.FileSystemListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.info.BuildProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class BeanProvider {

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
  @Value("\${miftp.ftp.removeEmptyDirs:false}")
  var removeEmptyDirs: Boolean = false
  @Value("\${miftp.ftp.cleanupInterval}")
  var cleanupInterval: Int = 10
  @Value("\${miftp.maxTokenFiles:10}")
  var maxTokenFiles: Int = 10
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
  fun server(ftpFileStore: FtpFileStore): MiFtpServer {
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
            .removeEmptyDirs(removeEmptyDirs)
            .fileSystemListener(getFtpEventListener(ftpFileStore))
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

  @Bean
  fun fileStore(): FtpFileStore {
    return FtpFileStore(maxTokenFiles)
  }

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

  //
  // below only internal used methods (no bean provider methods)

  private fun handleInfo(buildProperties: BuildProperties) {
    println("build version is <" + buildProperties.version + ">")
    println("build time is <" + buildProperties.time + ">")

//    buildInfo = BuildInfo(buildProperties.version, buildProperties.time.toString())
    buildInfo.version = buildProperties.version
    buildInfo.timestamp = buildProperties.time.toString()
//    println("value for custom key 'foo' is <" + buildProperties.get("foo") + ">")
  }


  fun getUsername() = username!!

  private fun getFtpEventListener(fileStore: FtpFileStore): FileSystemListener {
    return when (eventListener) {
      "" -> FileSystemListener { }
      "SlackNotifier" -> SlackNotifier().init(eventListenerParameters, fileStore)
      "SlackImageDiffNotifier" -> SlackImageDiffNotifier().init(eventListenerParameters, fileStore)
      else -> handleMissingEventListener()
    }
  }

  private fun handleMissingEventListener(): FileSystemListener {
    if (failOnMissingEventListener) {
      throw IllegalStateException("ERROR: Configured event listener $eventListener is not available.")
    }
    return FileSystemListener {
      println("WARNING: Configured event listener $eventListener is not available. (Received an event at ${it.timestamp})")
    }
  }
}