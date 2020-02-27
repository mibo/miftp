package de.mirb.project.miftp.image

import java.io.InputStream

class RectangleSelectionImageComparator(private val imageSelector: ImageSelector, sensitivity: Double = 0.1): ImageComparator(sensitivity) {

  override fun compare(firstImage: InputStream, secondImage: InputStream): Double {
    return compare(firstImage, secondImage, imageSelector)
  }
}