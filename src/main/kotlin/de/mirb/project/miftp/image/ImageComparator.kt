package de.mirb.project.miftp.image

import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

class ImageComparator {
  fun compare(firstImage: InputStream, secondImage: InputStream): Double {

    val imageOne = ImageIO.read(firstImage)
    val imageTwo = ImageIO.read(secondImage)

    val matrix = populateTheMatrixOfTheDifferences(imageOne, imageTwo)
    return percentageOfZeros(matrix)
  }

  private fun percentageOfZeros(matrix: Array<IntArray>): Double {
    val all = matrix[0].size * matrix.size
    var zeros = 0
    matrix.forEach { it.forEach { if (it == 0) zeros++ } }
    return if (zeros == 0) 0.0 else (zeros / all).toDouble()
  }

  /**
   * Populate binary matrix by "0" and "1". If the pixels are difference set it as "1", otherwise "0".
   *
   * @param firstImage [BufferedImage] object of the first image.
   * @param secondImage [BufferedImage] object of the second image.
   * @return populated binary matrix.
   */
  fun populateTheMatrixOfTheDifferences(firstImage: BufferedImage, secondImage: BufferedImage): Array<IntArray> {
    val matrix = Array(firstImage.width) { IntArray(firstImage.height) }
    for (y in 0 until firstImage.height) {
      for (x in 0 until firstImage.width) {
        matrix[x][y] = if (isDifferent(firstImage.getRGB(x, y), secondImage.getRGB(x, y))) 1 else 0
      }
    }
    return matrix
  }

  /**
   * Say if the two pixels equal or not. The rule is the difference between two pixels
   * need to be more then 10%.
   *
   * @param rgb1 the RGB value of the Pixel of the Image1.
   * @param rgb2 the RGB value of the Pixel of the Image2.
   * @return `true` if they' are difference, `false` otherwise.
   */
  fun isDifferent(rgb1: Int, rgb2: Int): Boolean {
    val red1 = rgb1 shr 16 and 0xff
    val green1 = rgb1 shr 8 and 0xff
    val blue1 = rgb1 and 0xff
    val red2 = rgb2 shr 16 and 0xff
    val green2 = rgb2 shr 8 and 0xff
    val blue2 = rgb2 and 0xff
    val result = Math.sqrt(Math.pow((red2 - red1).toDouble(), 2.0) +
            Math.pow((green2 - green1).toDouble(), 2.0) +
            Math.pow((blue2 - blue1).toDouble(), 2.0)) / Math.sqrt(Math.pow(255.0, 2.0) * 3)
    return result > 0.1
  }
}