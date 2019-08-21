package de.mirb.project.miftp.control.notifier

import de.mirb.project.miftp.fs.listener.FileSystemEvent
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test

class SlackNotifierTest {

  val mandatoryInitParameters = mapOf("slack_webhook_url" to "http://")

  @Test
  fun initFilterEmpty() {
    val notifier = SlackNotifier()
    notifier.init(mapOf("slack_webhook_url" to "http://"))
    //val contains = notifier.filter.containsAll(listOf(FileSystemEvent.EventType.CREATED, FileSystemEvent.EventType.DELETED))
    assertThat(notifier.filter, empty())
  }

  @Test
  fun initFilterSuccess() {
    val notifier = SlackNotifier()
    notifier.init(mandatoryInitParameters.plus("event_filter" to "CREATED, DELETED"))
    //val contains = notifier.filter.containsAll(listOf(FileSystemEvent.EventType.CREATED, FileSystemEvent.EventType.DELETED))
    assertThat(notifier.filter, contains(FileSystemEvent.EventType.CREATED, FileSystemEvent.EventType.DELETED))
    assertThat(notifier.filter, not(contains(FileSystemEvent.EventType.MODIFIED)))
  }

  @Test
  fun initFilterSuccessInvalid() {
    val notifier = SlackNotifier()
    try {
      notifier.init(mandatoryInitParameters.plus("event_filter" to "CREATED, UNKNOWN, DELETED"))
      fail("Expected exception was not thrown")
    } catch (e: IllegalArgumentException) {
      assertThat(e.message, endsWith("No enum constant de.mirb.project.miftp.fs.listener.FileSystemEvent.EventType.UNKNOWN"))
    }
  }
}