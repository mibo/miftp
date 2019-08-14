package de.mirb.project.miftp.image

import org.junit.Assert
import org.junit.Test
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.*

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
  fun differentImagesDifferentSize() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_three.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(0.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun sameImagesDifferentSize() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
//    val secondImage = resize(loadImageResource("images/image_three.jpg"), 2.0, 2.0)
    val secondImage = loadImageResource("images/image_three_small.jpg")

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
    return Optional.ofNullable(Thread.currentThread().contextClassLoader.getResourceAsStream(resourceName))
            .orElseThrow { IllegalArgumentException("Requested test resource '$resourceName' not found in classpath") }
  }

//  private fun resize(image: InputStream, heightFactor: Double, widthFactor: Double): ImageInputStream? {
//    val img = ImageIO.read(image)
//    val newHeight = heightFactor * img.height
//    val newWidth = widthFactor * img.width
//
//    return ImageIO.createImageInputStream(resize(img, newHeight.toInt(), newWidth.toInt()))
//  }

  private fun resize(img: BufferedImage, height: Int, width: Int): BufferedImage {
    val tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = resized.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()
    return resized
  }
}