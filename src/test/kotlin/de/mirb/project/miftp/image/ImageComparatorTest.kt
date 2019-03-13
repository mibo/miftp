package de.mirb.project.miftp.image

import org.junit.Assert
import org.junit.Test
import java.io.InputStream

class ImageComparatorTest {

  @Test
  fun differentImages() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_two.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(0.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun sameImages() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_one.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(1.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  private fun loadImageResource(resourceName: String): InputStream {
    return Thread.currentThread().contextClassLoader.getResourceAsStream(resourceName)
  }
}