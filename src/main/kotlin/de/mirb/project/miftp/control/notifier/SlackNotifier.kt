package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.fs.listener.FileSystemEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import java.time.ZoneId

class SlackNotifier : FtpEventListener {

  val PARA_WEBHOOK_URL = "url"
  val PARA_FILTER = "filter"

  lateinit var url: String
  lateinit var filter: Set<FileSystemEvent.EventType>

  override fun init(parameters: Map<String, String>): FtpEventListener {
    url = getParameterOr(parameters, PARA_WEBHOOK_URL)
    filter = createFilterSet(parameters)
    return this
  }

  private fun createFilterSet(parameters: Map<String, String>): Set<FileSystemEvent.EventType> {
    // TODO: re-think if wrong parameters should cause an exception:
    // `java.lang.IllegalArgumentException: No enum constant ....`
    val filterParameter = parameters[PARA_FILTER]
    return if(filterParameter.isNullOrEmpty()) {
      HashSet()
    } else {
      filterParameter.split(",")
                      .map { FileSystemEvent.EventType.valueOf(it.trim()) }.toSet()
    }
  }

  private fun getParameterOr(parameters: Map<String, String>, key: String) : String {
//    val value = parameters[key]
    return parameters.getOrElse(key, { "" })
  }

  override fun fileSystemChanged(event: FileSystemEvent) {
    if(filter.isEmpty() || filter.contains(event.type)) {
      val jsonContent = createJsonPostContent(event)
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

  private fun createJsonPostContent(event: FileSystemEvent) : String {
    val message = "${event.user.name} has ${event.type.name} the path ${event.file.absolutePath} at ${event.timestamp}"
    return """
    {
    "attachments": [
        {
            "fallback": "$message",
            "color": "#36a64f",
            "author_name": "MiFtp server: ${event.user.name}",
            "author_link": "http://.../",
            "title": "${event.type.name}: ${event.file.name}",
            "title_link": "https://api.slack.com/",
            "text": "$message",
            "footer": "MiFtp",
            "ts": ${event.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond()}
        }
      ]
    }
    """.trimIndent()
  }
}
