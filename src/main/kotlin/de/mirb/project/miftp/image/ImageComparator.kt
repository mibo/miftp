package de.mirb.project.miftp.image

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @param sensibility The default is the difference between two pixels need to be more than 10% (=> `0.1`).
 */
class ImageComparator(val sensibility: Double = 0.1) {

  /**
   * ```
   * p1x,p1y --------------------- p4x,p4y
   * |                                   |
   * |                                   |
   * |                                   |
   * |                                   |
   * |                                   |
   * p2x,p2y --------------------- p3x,p3y
   * ```
   * Default values are: p1x: 0; p1y: 1
   * Default values are: p2x: 0; p2y: 0
   * Default values are: p3x: 1; p3y: 0
   * Default values are: p4x: 1; p4y: 1
   */
  @Suppress("GrazieInspection")
  data class ImageSelector(val p1x: Double = 0.0, val p1y: Double = 1.0,
                           val p2x: Double = 0.0, val p2y: Double = 0.0,
                           val p3x: Double = 1.0, val p3y: Double = 0.0,
                           val p4x: Double = 1.0, val p4y: Double = 1.0) {

    fun verify(): Boolean {
      return p1x <= p4x && p2x <= p3x && p1y >= p2y && p3y <= p4y
    }
  }

  /**
   * ```
   * ----------------------------- p2x,p2y
   * |                                   |
   * |                                   |
   * |                                   |
   * |                                   |
   * |                                   |
   * p1x,p1y -----------------------------
   * ```
   * @param p1x
   * @param p1y
   * @param p2x
   * @param p2y
   */
  @Suppress("GrazieInspection")
  data class ImageSelectorRectangle(val p1x: Double = 0.0, val p1y: Double = 0.0,
                           val p2x: Double = 1.0, val p2y: Double = 1.0)

  /**
   * Compare how equal both images are. The result is a value between 0 and 1.0
   * which results in the percentage (0-100%) of equality.
   *
   * @param firstImage
   * @param secondImage
   * @return comparision how equal both images are as percentage (0.0=0% to 1.0=100%)
   */
  fun compare(firstImage: InputStream, secondImage: InputStream,
              selector: ImageSelectorRectangle = ImageSelectorRectangle()): Double {

    val imageOne = ImageIO.read(firstImage)
    val imageTwo = ImageIO.read(secondImage)

    val matrix = populateTheMatrixOfTheDifferences(imageOne, imageTwo, selector)
    return percentageOfZeros(matrix, selector)
  }

  /**
   * Compare how equal both images are. The result is a value between 0 and 1.0
   * which results in the percentage (0-100%) of equality.
   *
   * @param firstImage
   * @param secondImage
   * @return comparision how equal both images are as percentage (0.0=0% to 1.0=100%)
   */
  fun compare(firstImage: InputStream, secondImage: InputStream,
              selector: ImageSelector): Double {

    require(selector.verify()) { "Select $selector with invalid values" }

    val imageOne = ImageIO.read(firstImage)
    val imageTwo = ImageIO.read(secondImage)

    val matrix = populateTheMatrixOfTheDifferences(imageOne, imageTwo, selector)
    return percentageOfZeros(matrix, selector)
  }

  private fun percentageOfZeros(matrix: Array<IntArray>, selector: ImageSelector): Double {
    val all = matrix[0].size * matrix.size

    val zeros = all - matrix.fold(0) { ones, outer ->
      outer.fold(ones) { inOnes, value -> inOnes + value }
    }
    return if (zeros == 0) 0.0 else (zeros.div(all.toDouble()))
  }

  private fun percentageOfZeros(matrix: Array<IntArray>, selector: ImageSelectorRectangle): Double {
    val height = matrix[0].size
    val width = matrix.size
    val startHeight = (selector.p1y * height).toInt()
    val endHeight = (selector.p2y * height).toInt()
    val startWidth = (selector.p1x * width).toInt()
    val endWidth = (selector.p2x * width).toInt()

    val all = (endHeight - startHeight) * (endWidth - startWidth)

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
//    val startHeight = (selector.p1y * firstImageCompare.height).toInt()
//    val endHeight = (selector.p2y * firstImageCompare.height).toInt()
    val minWidth = (selector.p1x.coerceAtMost(selector.p2x) * firstImageCompare.width).toInt()
    val maxWidth = (selector.p3x.coerceAtLeast(selector.p4x) * firstImageCompare.width).toInt()

    val funP12 = createLinearFunction(
            selector.p1x * firstImageCompare.width,
            selector.p1y * firstImageCompare.height,
            selector.p2x * firstImageCompare.width,
            selector.p2y * firstImageCompare.height)
    val funP23 = createLinearFunction(
            selector.p2x * firstImageCompare.width,
            selector.p2y * firstImageCompare.height,
            selector.p3x * firstImageCompare.width,
            selector.p3y * firstImageCompare.height)
    val funP14 = createLinearFunction(
            selector.p1x * firstImageCompare.width,
            selector.p1y * firstImageCompare.height,
            selector.p4x * firstImageCompare.width,
            selector.p4y * firstImageCompare.height)
    val funP34 = createLinearFunction(
            selector.p3x * firstImageCompare.width,
            selector.p3y * firstImageCompare.height,
            selector.p4x * firstImageCompare.width,
            selector.p4y * firstImageCompare.height)


    val p1xmax = selector.p1x * firstImageCompare.width
    val p4xmax = selector.p4x * firstImageCompare.width
    println("Compare x from $minWidth->$maxWidth with p1max: $p1xmax; p4max: $p4xmax")
    for (x in minWidth until maxWidth) {
      val startHeight = funP23.calculateY(x).toInt()
      println("funP12: " + (x < p1xmax) + "; funP14: " + (x < p4xmax) + " else funP34")
      val endHeight = when {
        x < p1xmax -> funP12.calculateY(x)
        x < p4xmax -> funP14.calculateY(x)
        else -> funP34.calculateY(x)
      }.toInt()
      print("\nx:$x (y:$startHeight->$endHeight) => ")
      for (y in startHeight until endHeight) {
//        print("$y,")
        matrix[x][y] = if (isDifferent(firstImageCompare.getRGB(x, y), secondImageCompare.getRGB(x, y))) 1 else 0
        print(matrix[x][y].toString() + ",")
      }
    }
    return matrix
  }

  // f(x) = m * x + n = y | p (x,y)
  fun createLinearFunction(p1x: Double, p1y: Double, p2x: Double, p2y:Double): LinearFunction {

//    if (p1x == p2x) {
//
//    }
//    val m = (p1y - p2y) / (p1x - p2x)

    // m =
    val m: Double =
            if (p1x == p2x) 0.0 else (p1y - p2y) / (p1x - p2x)
    // n = y2 - m * x1
    val n = p2y - m * p1x

    return LinearFunction(m, n)
  }

  class LinearFunction(private val m: Double, private val n: Double) {
//    fun calculateY(x: Int): Double = m * x + n
    fun calculateY(x: Int): Double {
      println("Calculate with '$m * $x + $n'")
      return m * x + n
    }
  }

  /**
   * Populate binary matrix by "0" and "1". If the pixels are difference set it as "1", otherwise "0".
   *
   * @param firstImage [BufferedImage] object of the first image.
   * @param secondImage [BufferedImage] object of the second image.
   * @return populated binary matrix.
   */
  private fun populateTheMatrixOfTheDifferences(firstImage: BufferedImage, secondImage: BufferedImage,
                                                selector: ImageSelectorRectangle): Array<IntArray> {
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
    val startHeight = (selector.p1y * firstImageCompare.height).toInt()
    val endHeight = (selector.p2y * firstImageCompare.height).toInt()
    val startWidth = (selector.p1x * firstImageCompare.width).toInt()
    val endWidth = (selector.p2x * firstImageCompare.width).toInt()

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