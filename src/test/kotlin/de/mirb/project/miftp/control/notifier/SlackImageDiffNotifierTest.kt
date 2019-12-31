package de.mirb.project.miftp.control.notifier

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class SlackImageDiffNotifierTest {

  private val notifier = SlackImageDiffNotifier()

  @Test
  fun createImageSelectorNoneSet() {
    val params = "p1[:];p2[:];p3[:];p4[:]"

    val selector = notifier.createImageSelector(params)
    assertEquals(0.0, selector.p1x)
    assertEquals(1.0, selector.p1y)
    assertEquals(0.0, selector.p2x)
    assertEquals(0.0, selector.p2y)
    assertEquals(1.0, selector.p3x)
    assertEquals(0.0, selector.p3y)
    assertEquals(1.0, selector.p4x)
    assertEquals(1.0, selector.p4y)
  }

  @Test
  fun createImageSelectorPartlySet() {
    val params = "p1[:];p2[1.0:];p3[:0.7];p4[0.0:1.0]"

    val selector = notifier.createImageSelector(params)
    assertEquals(0.0, selector.p1x)
    assertEquals(1.0, selector.p1y)
    assertEquals(1.0, selector.p2x)
    assertEquals(0.0, selector.p2y)
    assertEquals(1.0, selector.p3x)
    assertEquals(0.7, selector.p3y)
    assertEquals(0.0, selector.p4x)
    assertEquals(1.0, selector.p4y)
  }

  @Test
  fun createImageSelectorFullSet() {
    val params = "p1[0.2:0.8];p2[0.3:0.4];p3[0.9:0.7];p4[0.6:0.5]"

    val selector = notifier.createImageSelector(params)
    assertEquals(0.2, selector.p1x)
    assertEquals(0.8, selector.p1y)
    assertEquals(0.3, selector.p2x)
    assertEquals(0.4, selector.p2y)
    assertEquals(0.9, selector.p3x)
    assertEquals(0.7, selector.p3y)
    assertEquals(0.6, selector.p4x)
    assertEquals(0.5, selector.p4y)
  }
}