package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.fs.listener.FileSystemEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import java.time.ZoneId

class SlackNotifier : FtpEventListener {

  val PARA_WEBHOOK_URL = "url"

  lateinit var url: String

  override fun init(parameters: Map<String, String>): FtpEventListener {
    url = getParameterOr(parameters, PARA_WEBHOOK_URL)
    return this
  }

  private fun getParameterOr(parameters: Map<String, String>, key: String) : String {
//    val value = parameters[key]
    return parameters.getOrElse(key, { "" })
  }

  override fun fileSystemChanged(event: FileSystemEvent) {
    val jsonContent = createJsonPostContent(event)
    slackPost(jsonContent)
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
