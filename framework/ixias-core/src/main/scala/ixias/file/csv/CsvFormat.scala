/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.file.csv

trait CsvFormat
{
  val ASCII_NULL            = 0x00
  val ASCII_HTAB            = 0x09
  val ASCII_VTAB            = 0x0B
  val ASCII_LINE_FEED       = 0x0A
  val ASCII_CARRIAGE_RETURN = 0x0D
  val ASCII_SPACE           = 0x20

  val CSV_FIELD_TERM_CHAR   = '\t'
  val CSV_ENCLOSED_CHAR     = '"'
  val CSV_ESCAPED_CHAR      = '\\'

  val CSV_WRITE_LINE_TERM   = "\r\n"
  val CSV_WRITE_QUOTE_STYLE: QuoteStyle = QuoteStyle.NONE_NUMERIC
  val CSV_WRITE_MINIMAL_QUOTE_SPECS = Seq(
    '\r',
    '\n',
    CSV_FIELD_TERM_CHAR,
    CSV_ENCLOSED_CHAR
  )
}

object CsvDefaultFormat extends CsvFormat
