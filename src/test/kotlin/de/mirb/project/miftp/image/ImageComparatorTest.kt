package de.mirb.project.miftp.image

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class ImageComparatorTest {


  /**
   * Just for local test of comparison of images.
   * Read all files based on given name pattern and prints differences (to console).
   * **Does not do any assertions** (hence the @Ignore is set)
   */
  @Test
  @Ignore("Only for local tests...")
  fun realImagesRow() {
    val compare = ImageComparator()

//    val namePattern = "images/_AAA_%d.jpg"
    val namePattern = "images/_test_image_%d.jpg"
    var count = 1
    val filenames = generateSequence {
      val filename = String.format(namePattern, count++)
      val url = Thread.currentThread().contextClassLoader.getResource(filename)
      if (url == null) {
        null
      } else {
        filename
      }
    }.toList()
//    println(filenames)

    val initial: Pair<String, String> = Pair(filenames[0], "\n1) -> 2)")
    val t = filenames.drop(1).foldIndexed(initial) {
      index, pairElement, nextImageName ->
        val firstImage = loadImageResource(pairElement.first)
        val secondImage = loadImageResource(nextImageName)

        val result = compare.compare(firstImage, secondImage)
        Pair(nextImageName, "${pairElement.second} => $result; (first=${pairElement.first}))" +
                "\n${index+2}) -> ${index+3})")
    }

    println("Result:\n$t.second")
 }

  @Test
  @Ignore("Need different images")
  fun realImagesRow_FAIL() {
    val compare = ImageComparator()

//    val namePattern = "images/_AAA_%d.jpg"
    val namePattern = "images/_test_image_%d.jpg"
//    val r = Stream.iterate(1, { String.format(namePattern, it + 1) })
//            .limit(10)
//            .collect(Collectors.toList())
//    println(r)
    var count = 1
    val imagesResources: MutableList<InputStream> = ArrayList()
    while (count > 0) {
      val ir = loadImageResourceOptional(String.format(namePattern, count++))

      if(ir.isPresent) {
        imagesResources.add(ir.get())
      } else {
        count = -1
      }
    }
//    println(imagesResources)

    val initial: Pair<InputStream, String> = Pair(imagesResources.removeAt(0), "1) -> 2)")
    val t = imagesResources.foldIndexed(initial) {
      index, pairElement, nextImage ->
        println("Processing $index" )
        val result = compare.compare(pairElement.first, nextImage)
        Pair(nextImage, pairElement.second + " => $result;\n${index+2}) -> ${index+3})")
    }

    println(t.second)
//    do {
//      String.format(namePattern, 1)
//      val ir = loadImageResource(String.format(namePattern, 1))
//
//    } while (true)
//    println(String.format(namePattern, 1))
  }

  @Test
  fun mathStuff() {
    val compare = ImageComparator()
    val function = compare.createLinearFunction(140.0, 30.0, 200.0, 70.0)
    val x = 160
    println("f($x) = " + function.calculateY(x).toInt())
    val x1 = 140
    println("f($x1) = " + function.calculateY(x1).toInt())
    val x2 = 200
    println("f($x2) = " + function.calculateY(x2).toInt())
    Assert.assertEquals(43, function.calculateY(160).toInt())
    Assert.assertEquals(30, function.calculateY(140).toInt())
    Assert.assertEquals(69, function.calculateY(200).toInt())
  }

  @Test
  fun mathStuffBasic() {
    val compare = ImageComparator()
    val function = compare.createLinearFunction(20.0, 20.0, 60.0, 60.0)
    val x = 160
    println("f($x) = " + function.calculateY(x).toInt())
    val x1 = 140
    println("f($x1) = " + function.calculateY(x1).toInt())
    val x2 = 200
    println("f($x2) = " + function.calculateY(x2).toInt())
    Assert.assertEquals(160, function.calculateY(160).toInt())
    Assert.assertEquals(140, function.calculateY(140).toInt())
    Assert.assertEquals(200, function.calculateY(200).toInt())
  }

  @Test
  fun verifyInvalidSelector() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_two.jpg")

    assertThrows<java.lang.IllegalArgumentException> {
      compare.compare(firstImage, secondImage, ImageComparator.ImageSelector(p1x = 0.7, p4x = 0.2)) }
    assertThrows<java.lang.IllegalArgumentException> {
      compare.compare(firstImage, secondImage, ImageComparator.ImageSelector(p1y = 0.1, p2y = 0.2)) }
  }


  @Test
  fun differentImages() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_two.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(0.18074544270833334, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun slightlyDifferentImages() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
    val secondImage = loadImageResource("images/image_four.jpg")

    val selector = ImageComparator.ImageSelector(p3x = 0.5, p4x = 0.5)
    val result = compare.compare(firstImage, secondImage, selector)

    Assert.assertEquals(1.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun slightlyDifferentImagesTriangle() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
    val secondImage = loadImageResource("images/image_four.jpg")

    val selector = ImageComparator.ImageSelector(p4x = 0.5, p4y = 0.7, p3y = 0.5)
    val result = compare.compare(firstImage, secondImage, selector)

    Assert.assertEquals(1.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun slightlyDifferentImagesOnlyDiffSelected() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
    val secondImage = loadImageResource("images/image_four.jpg")

    val selector = ImageComparator.ImageSelector(p1x = 0.7, p2x = 1.0, p2y = 0.5, p3y = 0.5)
    val result = compare.compare(firstImage, secondImage, selector)

    Assert.assertEquals(0.95484375, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun slightlyDifferentImagesHalfSelected() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
    val secondImage = loadImageResource("images/image_four.jpg")

    val selector = ImageComparator.ImageSelectorRectangle(p2x = 0.5)
    val result = compare.compare(firstImage, secondImage, selector)

    Assert.assertEquals(1.0, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun slightlyDifferentImagesPartlySelectedWithDiff() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
    val secondImage = loadImageResource("images/image_four.jpg")

    val selector = ImageComparator.ImageSelectorRectangle(p2x = 0.9)
    val result = compare.compare(firstImage, secondImage, selector)

    Assert.assertEquals(0.9587094907407407, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun differentImagesDifferentSize() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_one.jpg")
    val secondImage = loadImageResource("images/image_three.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(0.2001953125, result, 0.0)
    Assert.assertNotNull(result)
  }

  @Test
  fun sameImagesDifferentSize() {
    val compare = ImageComparator()
    val firstImage = loadImageResource("images/image_three.jpg")
//    val secondImage = resize(loadImageResource("images/image_three.jpg"), 2.0, 2.0)
    val secondImage = loadImageResource("images/image_three_small.jpg")

    val result = compare.compare(firstImage, secondImage)

    Assert.assertEquals(0.9877994791666667, result, 0.0)
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

  private fun loadImageResourceOptional(resourceName: String): Optional<InputStream> {
    return Optional.ofNullable(Thread.currentThread().contextClassLoader.getResourceAsStream(resourceName))
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