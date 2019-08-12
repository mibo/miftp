package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.fs.listener.FileSystemEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.time.ZoneId

class SlackNotifier : FtpEventListener {

  val PARA_WEBHOOK_URL = "slack_webhook_url"
  val PARA_EVENT_FILTER = "event_filter"
  val PARA_MIFTP_SERVER_BASE_URL = "miftp_server_base_url"

  lateinit var url: String
  lateinit var filter: Set<FileSystemEvent.EventType>
  lateinit var serverBaseUrl: String

  override fun init(parameters: Map<String, String>): FtpEventListener {
    url = getOrThrow(parameters, PARA_WEBHOOK_URL)
    serverBaseUrl = createServerBaseUrl(parameters)
    filter = createFilterSet(parameters)
    return this
  }

  private fun getOrThrow(parameters: Map<String, String>, key: String) =
    parameters.getOrElse(key, { throw IllegalArgumentException("SlackNotifier must have a $key set") })

  private fun createServerBaseUrl(parameters: Map<String, String>): String {
    return parameters.getOrElse(PARA_MIFTP_SERVER_BASE_URL, {
      return InetAddress.getLocalHost().hostName + parameters.get("server.port")
    })
  }

  private fun createFilterSet(parameters: Map<String, String>): Set<FileSystemEvent.EventType> {
    // TODO: re-think if wrong parameters should cause an exception:
    // `java.lang.IllegalArgumentException: No enum constant ....`
    val filterParameter = parameters[PARA_EVENT_FILTER]
    return if(filterParameter.isNullOrEmpty()) {
      HashSet()
    } else {
      filterParameter.split(",")
                      .map { FileSystemEvent.EventType.valueOf(it.trim()) }.toSet()
    }
  }

  override fun fileSystemChanged(event: FileSystemEvent) {
    if(filter.isEmpty() || filter.contains(event.type)) {
      val jsonContent = createJsonPostContent(event, serverBaseUrl)
      slackPost(jsonContent)
    }
  }

  private fun slackPost(message: String) {
    val client = HttpClient()
    runBlocking {
      val htmlContent = client.post<String>(url) {
        body = message
      }
      println("Result $htmlContent")
    }
  }

  private fun createJsonPostContent(event: FileSystemEvent, baseUrl: String) : String {
    val message = "${event.user.name} has ${event.type.name} the path ${event.file.absolutePath} at ${event.timestamp}"
    return """
    {
    "attachments": [
        {
            "fallback": "$message",
            "color": "#36a64f",
            "author_name": "MiFtp server: ${event.user.name}",
            "author_link": "$baseUrl",
            "title": "${event.type.name}: ${event.file.name}",
            "title_link": "$baseUrl/files/${event.file.name}/content",
            "text": "$message",
            "footer": "MiFtp",
            "ts": ${event.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond()}
        }
      ]
    }
    """.trimIndent()
  }
}
