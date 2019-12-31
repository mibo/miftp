package de.mirb.project.miftp.image

import java.io.InputStream

class RectangleSelectionImageComparator(private val imageSelector: ImageSelector, sensibility: Double = 0.1): ImageComparator(sensibility) {

  override fun compare(firstImage: InputStream, secondImage: InputStream): Double {
    return compare(firstImage, secondImage, imageSelector)
  }
}