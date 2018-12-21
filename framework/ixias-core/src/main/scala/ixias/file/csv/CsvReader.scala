/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.file.csv

import scala.io.Source

object CsvReader
{
  def apply(filename: String, encoding: String = "UTF-8")
    (implicit format: CsvFormat = CsvDefaultFormat): CsvReader =
    CsvReader(Source.fromFile(filename, encoding), format)
}

case class CsvReader(
  private val source:  Source,
  private val format: CsvFormat
) {
  val lines:   Seq[String]      = initLines()
  val columns: Seq[Seq[String]] = initColumns()

  /** Reads all lines. */
  def initLines(): Seq[String] = {
    var buff     = ""
    var previous = 0
    var lines    = Seq[String]()
    var state    = 0
    var enclosed = false
    source.foldLeft(0){ (pos, current) => {
      // reached at the end of the row (CR,LF,CR+LF)
      if (!enclosed && (current.toInt == format.ASCII_LINE_FEED
                     || current.toInt == format.ASCII_CARRIAGE_RETURN)) {
        if (0 < buff.length) { lines :+= buff }
        buff     = ""
        state    = 0
        enclosed = false
      } else {
        // check enclosed content data.
        current.toInt match {
          // marks the beginning of the enclosed
          case format.CSV_ENCLOSED_CHAR =>
            if (previous != format.CSV_ESCAPED_CHAR) {
              state += 1
              enclosed = (state == 1)
            }
          // found the end of the current field.
          case format.CSV_FIELD_TERM_CHAR =>
            if (!enclosed && previous != format.CSV_ESCAPED_CHAR) {
              state    = 0
              enclosed = false
            }
          case format.ASCII_NULL  =>
          case format.ASCII_HTAB  =>
          case format.ASCII_VTAB  =>
          case format.ASCII_SPACE =>
          case _ => if (0 < state) {
            enclosed = true
          }
        }
      }
      buff    += current
      previous = current.toInt
      pos + 1
    }}
    if (0 < buff.length) { lines :+= buff }
    source.close()
    lines
  }

  /** Parses a given CSV file. */
  def initColumns(): Seq[Seq[String]] = {
    lines.foldLeft(Seq[Seq[String]]()){ (rows, line) =>
      var buff     = ""
      var previous = 0
      var state    = 0
      var enclosed = false
      var row      = Seq[String]()
      line.foldLeft(0) { (pos, current) =>
        // check whether reached at the end of field character
        var edge = false
        if (!enclosed) {
          if  (current.toInt == format.ASCII_LINE_FEED
            || current.toInt == format.ASCII_CARRIAGE_RETURN) {
            edge = true
          }
          if  (current.toInt == format.CSV_FIELD_TERM_CHAR
            && previous      != format.CSV_ESCAPED_CHAR) {
            edge = !enclosed
          }
        }
        // the end of field character
        if (edge) {
          // If the space and enclosure char is found at either ends of the string
          pickupValue(buff).foreach { v => row :+= v }
          buff     = ""
          state    = 0
          enclosed = false
        }
        // check enclosed content data.
        current match {
          // marks the beginning of the enclosed
          case format.CSV_ENCLOSED_CHAR =>
            if (previous != format.CSV_ESCAPED_CHAR) {
              state += 1
              enclosed = (state == 1)
            }
          case format.ASCII_NULL  =>
          case format.ASCII_HTAB  =>
          case format.ASCII_VTAB  =>
          case format.ASCII_SPACE =>
          case _ => if (0 < state) {
            enclosed = true
          }
        }
        previous = current.toInt
        buff += current
        pos  +  1
      }
      pickupValue(buff).foreach {
        v => row :+= v
      }
      if (0 < row.length) rows :+ row else rows
    }
  }

  /** Pick up a single field value */
  private def pickupValue(input: String): Option[String] =
    input.length match {
      case 0 => None
      case _ => Some(input
          .stripPrefix(format.CSV_FIELD_TERM_CHAR.asInstanceOf[Char].toString)
          .trim
          .stripPrefix(format.CSV_ENCLOSED_CHAR.asInstanceOf[Char].toString)
          .stripSuffix(format.CSV_ENCLOSED_CHAR.asInstanceOf[Char].toString))
    }
}
