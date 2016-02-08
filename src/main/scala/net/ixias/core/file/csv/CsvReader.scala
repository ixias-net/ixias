/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.file.csv

import scala.io.Source
import scala.util.{ Try, Success, Failure }

case class CsvReader(
  private val source:  Source,
  private val format: CsvFormat
) {
  val lines:   Seq[String]      = init_lines()
  val columns: Seq[Seq[String]] = init_columns()

  def init_lines(): Seq[String] = {
    var buff     = ""
    var previous = 0
    var lines    = Seq[String]()
    var state    = 0
    var enclosed = false
    var l = 0
    source.foldLeft(0){ (pos, current) => {
      if (enclosed) {
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
          case _ if 0 < state => enclosed = true
        }
        buff += current
      } else {
        // reached at the end of the row (CR,LF,CR+LF)
        if (current.toInt == format.ASCII_CARRIAGE_RETURN
          ||current.toInt == format.ASCII_LINE_FEED) {
          if (0 < buff.length) {
            lines :+= buff
          }
          buff     = ""
          previous = 0
          state    = 0
          enclosed = false
        } else {
          buff += current
        }
      }
      previous = current.toInt
      pos + 1
    }}
    if (0 < buff.length) { lines :+= buff }
    source.close()
    lines
  }

  def init_columns(): Seq[Seq[String]] = {
    Seq(Seq[String]())
  }
}

object CsvReader {
  def apply(filename: String, encoding: String = "UTF-8")
    (implicit format: CsvFormat = CsvDefaultFormat): CsvReader =
    CsvReader(Source.fromFile(filename, encoding), format)
}
