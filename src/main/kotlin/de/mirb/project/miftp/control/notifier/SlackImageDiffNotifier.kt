package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.control.FtpFileStore
import de.mirb.project.miftp.fs.InMemoryFtpFile
import de.mirb.project.miftp.fs.listener.FileSystemEvent
import de.mirb.project.miftp.image.FourPointSelectionImageComparator
import de.mirb.project.miftp.image.ImageComparator
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import org.apache.ftpserver.ftplet.FtpFile
import org.slf4j.LoggerFactory.getLogger
import java.time.ZoneId

class SlackImageDiffNotifier : FtpEventListener {

  companion object {
    private val LOG = getLogger(SlackImageDiffNotifier::class.java)
  }

  val PARA_WEBHOOK_URL = "slack_webhook_url"
  val PARA_MIFTP_SERVER_BASE_URL = "miftp_server_base_url"
  /** all images which are less _equal_ (more _different_) then the threshold gets posted */
  val PARA_DIFF_THRESHOLD = "diff_threshold"
  val PARA_DIFF_THRESHOLD_DEFAULT = "0.5"
  /** all images which are less _equal_ (more _different_) then the ignored threshold gets ignored */
  val PARA_DIFF_IGNORE_THRESHOLD = "diff_ignore_threshold"
  val PARA_DIFF_IGNORE_THRESHOLD_DEFAULT = "0.0"
  // value must be a string in specified format
  // each point is defined as two double values in brackets separated with one colon
  // if a default double should be used the value can be omitted but each point must be defined
  // e.g. (`p1[:];p2[1.0:];p3[:0.7];p4[0.0:1.0]`)
  val PARA_IMAGE_SELECTOR_POINTS = "image_selector_points"

  private lateinit var url: String
  private lateinit var serverBaseUrl: String
  private var diffThreshold = PARA_DIFF_THRESHOLD_DEFAULT.toDouble()
  private var diffIgnore = PARA_DIFF_IGNORE_THRESHOLD_DEFAULT.toDouble()
  private var ftpFileStore: FtpFileStore? = null
  private var lastImage: FtpFile? = null
  private lateinit var imageComparator: ImageComparator


  override fun init(parameters: Map<String, String>, ftpFileStore: FtpFileStore): FtpEventListener {
    url = getOrThrow(parameters, PARA_WEBHOOK_URL)
    serverBaseUrl = createServerBaseUrl(parameters)
    diffThreshold = readDiffThreshold(parameters)
    diffIgnore = readDiffIgnoreThreshold(parameters)
    imageComparator = createImageComparator(parameters)

    this.ftpFileStore = ftpFileStore
    return this
  }

  private fun createImageComparator(parameters: Map<String, String>): ImageComparator {
    val imageSelectorParam = parameters[PARA_IMAGE_SELECTOR_POINTS]

    if (imageSelectorParam == null) {
      return ImageComparator()
    }

    val selector = createImageSelector(imageSelectorParam)
    return FourPointSelectionImageComparator(selector)
  }

  fun createImageSelector(imageSelectorParam: String): ImageComparator.ImageSelector {
    // init with default values
    val selectionPoints = doubleArrayOf(0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0)
    val points = imageSelectorParam.split(";")
    var index = 0
    //
    points.forEach {
      val twoDoubles = it.substring(2)
              .removeSurrounding("[", "]")
              .split(":")
      if (twoDoubles[0].isNotEmpty() && twoDoubles[0].toDoubleOrNull() != null) {
        selectionPoints[index] = twoDoubles[0].toDouble()
      }
      index++
      if (twoDoubles[1].isNotEmpty() && twoDoubles[1].toDoubleOrNull() != null) {
        selectionPoints[index] = twoDoubles[1].toDouble()
      }
      index++
    }

    return ImageComparator.ImageSelector(selectionPoints[0], selectionPoints[1], selectionPoints[2], selectionPoints[3],
            selectionPoints[4], selectionPoints[5], selectionPoints[6], selectionPoints[7])
  }

  private fun readDiffThreshold(parameters: Map<String, String>): Double {
    val diff = parameters.getOrDefault(PARA_DIFF_THRESHOLD, PARA_DIFF_THRESHOLD_DEFAULT).toDoubleOrNull()
    return diff ?: PARA_DIFF_THRESHOLD_DEFAULT.toDouble()
  }

  private fun readDiffIgnoreThreshold(parameters: Map<String, String>): Double {
    val diff = parameters.getOrDefault(PARA_DIFF_IGNORE_THRESHOLD, PARA_DIFF_IGNORE_THRESHOLD_DEFAULT).toDoubleOrNull()
    return diff ?: PARA_DIFF_IGNORE_THRESHOLD_DEFAULT.toDouble()
  }

  private fun getOrThrow(parameters: Map<String, String>, key: String) =
    parameters.getOrElse(key, { throw IllegalArgumentException("SlackNotifier must have a $key set") })

  private fun createServerBaseUrl(parameters: Map<String, String>): String {
    return parameters.getOrDefault(PARA_MIFTP_SERVER_BASE_URL, "...")
  }

  override fun fileSystemChanged(event: FileSystemEvent) {
    if(event.type == FileSystemEvent.EventType.CREATED) {
      if (isImage(event.file)) {
        handleFileCreatedEvent(event)
      }
    }
  }

  private fun handleFileCreatedEvent(event: FileSystemEvent) {
    if (lastImage != null) {
      compareFiles(lastImage!!, event.file).ifDifferentAndNotIgnored {
        val token = ftpFileStore!!.enableTokenBasedAccess(it.second)
        val jsonContent = createJsonPostContent(serverBaseUrl, event, it, token)
        slackPost(jsonContent)
      }
    }
    lastImage = event.file
    if (lastImage is InMemoryFtpFile) {
      (lastImage as InMemoryFtpFile).isLocked = true
    }
  }

  private fun isImage(file: FtpFile): Boolean {
    if(file.isDirectory) {
      return false
    }
    return file.name.endsWith("png", true)
            || file.name.endsWith("jpg", true)
            || file.name.endsWith("jpeg", true)
  }

  private fun compareFiles(first: FtpFile, second: FtpFile): DiffResult {
    val diff = imageComparator.compare(first.createInputStream(0), second.createInputStream(0))
    LOG.info("File compare (first=${first.name} to second=${second.name}): $diff (${diff < diffThreshold})")
    return DiffResult(first, second, diff, diffIgnore, diffThreshold)
  }

  private fun slackPost(message: String) {
    val client = HttpClient()
    runBlocking {
      LOG.debug("POST to $url")
      val htmlContent = client.post<String>(url) {
        body = message
      }
      LOG.debug("POST response $htmlContent")
      client.close()
    }
  }

  private fun createJsonPostContent(baseUrl: String, event: FileSystemEvent, diff: DiffResult, token: String) : String {
    val message = "(first=${diff.first.name} to second=${diff.second.name}): ${diff.diffValue} (${diff.isDifferent()})"

    return """
    {
    "attachments": [
        {
            "author_name": "MiFtp server: ${event.user.name}",
            "author_link": "$baseUrl",
            "pretext": "Different image created...",
            "fallback": "There is a difference between images ($message).",
            "title": "New image: ${diff.first.name}",
            "title_link": "$baseUrl/files/${diff.first.absolutePath}?content",
            "color": "#36a64f"
        },
        {
            "title": "Token link for: ${diff.first.name}",
            "title_link": "$baseUrl/go/token/${token}?content",
            "image_url": "$baseUrl/go/token/${token}?content"
        },
        {
            "title": "Previous image: ${diff.second.name}",
            "title_link": "$baseUrl/files/${diff.second.absolutePath}?content",
            "text": "Difference between images: ${diff.diffValue} (${diff.isDifferent()})",
            "color": "#003CA6",
            "footer": "MiFtp",
            "ts": ${event.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond()}
        }
      ]
    }
    """.trimIndent()
  }


  data class DiffResult(val first: FtpFile, val second: FtpFile,
                        val diffValue: Double, val diffIgnore: Double, val diffThreshold: Double) {

    fun isDifferent() = diffValue < diffThreshold
    fun ignore() = diffValue < diffIgnore

    fun ifDifferentAndNotIgnored(run: (r: DiffResult) -> Unit) {
      if(isDifferent() && !ignore()) {
        run(this)
      }
    }
  }
}
