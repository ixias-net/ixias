/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 *
 * -----------------------------------------------------------------------------
 *
 * Note:
 *  Reference of the source code from the following point of view.
 *  ・A pattern of object generation according to the type of file input / output.
 *
 *  https://github.com/tototoshi/scala-csv/blob/master/src/main/scala/com/github/tototoshi/csv/CSVWriter.scala
 *
 *  Copyright 2013 Toshiyuki Takahashi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ixias.file.csv

import java.io._

/**
 * The writer to output a csv file.
 */
case class CsvWriter(writer: Writer)(implicit format: CsvFormat)
    extends Closeable with Flushable {

  // --[ Properties ]-----------------------------------------------------------
  private [this] val underlying = new PrintWriter(writer)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Closes this stream and releases any system resources associated with it
   */
  def close(): Unit = underlying.close()

  /**
   * Flushes this stream by writing any buffered output to the underlying stream.
   */
  def flush(): Unit = underlying.flush

  // --[ Methods ]--------------------------------------------------------------
  /**
   * True to quote the field, otherwise false.
   */
  def shouldQuote(field: String, quote: QuoteStyle): Boolean =
    quote match {
      case QuoteStyle.ALL  => true
      case QuoteStyle.NONE => false
      case QuoteStyle.MINIMAL => {
        field.indexWhere(c =>
          format.CSV_WRITE_MINIMAL_QUOTE_SPECS.contains(c)
        ) != -1
      }
      case QuoteStyle.NONE_NUMERIC => {
        var foundDot = false
        field.indexWhere {
          case '.' if foundDot => true
          case '.' => foundDot =  true; false
          case c if c < '0'    => true
          case c if c > '9'    => true
          case _               => false
        } != -1
      }
    }

  /**
   * Writes the field to the CSV file. The field may get quotes added to it.
   */
  def writeField(field: String, shouldQuote: Boolean): Unit = {
    // A left enclosed caharcter.
    if (shouldQuote) {
      underlying.print(format.CSV_ENCLOSED_CHAR)
    }

    // Write a field content as string.
    field.map(c => {
      if ( (c == format.CSV_ENCLOSED_CHAR)
        || (c == format.CSV_FIELD_TERM_CHAR && format.CSV_WRITE_QUOTE_STYLE == QuoteStyle.NONE)) {
        underlying.print(format.CSV_ESCAPED_CHAR)
      }
      underlying.print(c)
    })

    // A right enclosed caharcter.
    if (shouldQuote) {
      underlying.print(format.CSV_ENCLOSED_CHAR)
    }
  }

  /**
   * Writes a single row to the CSV file.
   */
  def writeRow(fields: Seq[Any]): Unit = {
    fields.foldLeft(0)((pos, cur) => {
      if (0 < pos) {
        underlying.print(format.CSV_FIELD_TERM_CHAR)
      }
      writeField(
        cur.toString,
        shouldQuote(cur.toString, format.CSV_WRITE_QUOTE_STYLE)
      )
      pos + 1
    })
    underlying.print(format.CSV_WRITE_LINE_TERM)
    if (underlying.checkError) {
      throw new java.io.IOException("Failed to write a row to the CSV file.")
    }
  }
}


/**
 * The companion object for CsvWriter.
 */
object CsvWriter {

  implicit val defaultFormat: CsvFormat = CsvDefaultFormat
  val defaultEncoding = "UTF-16LE"

  // Create a new writer from a file name.
  def apply(file: String)                                    (implicit format: CsvFormat): CsvWriter = apply(file, false, defaultEncoding)(format)
  def apply(file: String, encoding: String)                  (implicit format: CsvFormat): CsvWriter = apply(file, false, encoding)(format)
  def apply(file: String, append: Boolean)                   (implicit format: CsvFormat): CsvWriter = apply(file, append, defaultEncoding)(format)
  def apply(file: String, append: Boolean, encoding: String) (implicit format: CsvFormat): CsvWriter = apply(new File(file), append, encoding)(format)

  // Create a new writer from a file resouce.
  def apply(file: File)                                      (implicit format: CsvFormat): CsvWriter = apply(file, false, defaultEncoding)(format)
  def apply(file: File, encoding: String)                    (implicit format: CsvFormat): CsvWriter = apply(file, false, encoding)(format)
  def apply(file: File, append: Boolean)                     (implicit format: CsvFormat): CsvWriter = apply(file, append, defaultEncoding)(format)
  def apply(file: File, append: Boolean, encoding: String)   (implicit format: CsvFormat): CsvWriter = apply(new FileOutputStream(file, append), encoding)(format)

  // Create a new writer from a output-stream.
  def apply(fos: OutputStream)                   (implicit format: CsvFormat): CsvWriter = apply(fos, defaultEncoding)(format)
  def apply(fos: OutputStream, encoding: String) (implicit format: CsvFormat): CsvWriter = {
    try {
      if (encoding == "UTF-8") {
        fos.write(0xef)
        fos.write(0xbb)
        fos.write(0xbf)
      }
      if (encoding == "UTF-16LE") {
        fos.write(0xff)
        fos.write(0xfe)
      }
      val writer = new OutputStreamWriter(fos, encoding)
      new CsvWriter(writer)(format)
    } catch {
      case e: UnsupportedEncodingException => fos.close(); throw e
    }
  }
}
