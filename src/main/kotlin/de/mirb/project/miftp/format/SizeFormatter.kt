package de.mirb.project.miftp.format

import java.text.DecimalFormat

class SizeFormatter {
  val kilobyte: Double = 1_024.0
  val megabyte = kilobyte * kilobyte
  val gigabyte = megabyte * kilobyte

  private val formatter = DecimalFormat("#.000")

  fun format(size: Long): String {
    return when {
      size >= gigabyte -> formatter.format(size / gigabyte) + " GByte"
      size >= megabyte -> formatter.format(size / megabyte) + " MByte"
      size >= kilobyte -> formatter.format(size / kilobyte) + " KByte"
      else -> "$size Byte"
    }
  }
}
