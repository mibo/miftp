package de.mirb.project.miftp.image

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @param sensibility The default is the difference between two pixels need to be more then 10% (=> `0.1`).
 */
class ImageComparator(val sensibility: Double = 0.1) {

  data class ImageSelector(val p1x: Double = 0.0, val p1y: Double = 1.0,
                           val p2x: Double = 0.0, val p2y: Double = 0.0,
                           val p3x: Double = 1.0, val p3y: Double = 0.0,
                           val p4x: Double = 1.0, val p4y: Double = 1.0)

  /**
   * Compare how equal both images are. The result is a value between 0 and 1.0
   * which results in the percentage (0-100%) of equality.
   *
   * @param firstImage
   * @param secondImage
   * @return comparision how equal both images are as percentage (0.0=0% to 1.0=100%)
   */
  fun compare(firstImage: InputStream, secondImage: InputStream,
              selector: ImageSelector = ImageSelector()): Double {

    val imageOne = ImageIO.read(firstImage)
    val imageTwo = ImageIO.read(secondImage)

    val matrix = populateTheMatrixOfTheDifferences(imageOne, imageTwo, selector)
    return percentageOfZeros(matrix)
  }

  private fun percentageOfZeros(matrix: Array<IntArray>): Double {
    val all = matrix[0].size * matrix.size
//    var zeros = 0
//    matrix.forEach { it.forEach { if (it == 0) zeros++ } }

    val zeros = all - matrix.fold(0) { ones, outer ->
      outer.fold(ones) { inOnes, value -> inOnes + value }
    }
    return if (zeros == 0) 0.0 else (zeros.div(all.toDouble()))
  }

  /**
   * Populate binary matrix by "0" and "1". If the pixels are difference set it as "1", otherwise "0".
   *
   * @param firstImage [BufferedImage] object of the first image.
   * @param secondImage [BufferedImage] object of the second image.
   * @return populated binary matrix.
   */
  private fun populateTheMatrixOfTheDifferences(firstImage: BufferedImage, secondImage: BufferedImage,
                                                selector: ImageSelector): Array<IntArray> {
    val heightDiff = firstImage.height != secondImage.height
    val widthDiff = firstImage.width != secondImage.width

    var firstImageCompare = firstImage
    var secondImageCompare = secondImage
    if(heightDiff || widthDiff) {
      // TODO: ...
      if(firstImage.height > secondImage.height) {
        secondImageCompare = resize(secondImage, firstImage.height, firstImage.width)
      } else {
        firstImageCompare = resize(firstImage, secondImage.height, secondImage.width)
      }
    }

    val matrix = Array(firstImageCompare.width) { IntArray(firstImageCompare.height) }
    val startHeight = (selector.p2y * firstImageCompare.height).toInt()
    val endHeight = (selector.p1y * firstImageCompare.height).toInt()
    val startWidth = (selector.p2x * firstImageCompare.width).toInt()
    val endWidth = (selector.p3x * firstImageCompare.width).toInt()

    for (y in startHeight until endHeight) {
      for (x in startWidth until endWidth) {
        matrix[x][y] = if (isDifferent(firstImageCompare.getRGB(x, y), secondImageCompare.getRGB(x, y))) 1 else 0
      }
    }
    return matrix
  }

  private fun resize(img: BufferedImage, height: Int, width: Int): BufferedImage {
    val tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = resized.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()
    return resized
  }

  /**
   * Say if the two pixels equal or not.
   * The rule is the difference between two pixels need to be more then 10%.
   *
   * @param rgb1 the RGB value of the Pixel of the Image1.
   * @param rgb2 the RGB value of the Pixel of the Image2.
   * @return `true` if they' are difference, `false` otherwise.
   */
  private fun isDifferent(rgb1: Int, rgb2: Int): Boolean {
    val red1 = rgb1 shr 16 and 0xff
    val green1 = rgb1 shr 8 and 0xff
    val blue1 = rgb1 and 0xff
    val red2 = rgb2 shr 16 and 0xff
    val green2 = rgb2 shr 8 and 0xff
    val blue2 = rgb2 and 0xff
    val result = sqrt((red2 - red1).toDouble().pow(2.0) +
            (green2 - green1).toDouble().pow(2.0) +
            (blue2 - blue1).toDouble().pow(2.0)) / sqrt(255.0.pow(2.0) * 3)
//    println("Diff rgb1 ($rgb1) with rgb2 ($rgb2): $result")
    return result > sensibility
  }
}