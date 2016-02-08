/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.file.csv

trait CsvFormat {
  val ASCII_NULL            = 0x00
  val ASCII_HTAB            = 0x09
  val ASCII_VTAB            = 0x0B
  val ASCII_LINE_FEED       = 0x0A
  val ASCII_CARRIAGE_RETURN = 0x0D
  val ASCII_SPACE           = 0x20

  val CSV_ENCLOSED_CHAR     = 0x22
  val CSV_FIELD_TERM_CHAR   = 0x2C
  val CSV_ESCAPED_CHAR      = 0x5C
}

object CsvDefaultFormat extends CsvFormat
