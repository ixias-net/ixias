/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.profile.{ SqlTableComponent => P }
import scala.language.implicitConversions

final case class SlickColumnOptionsExtension[A <: P#ColumnOptions](self: A) {
  val Boolean         = self.SqlType("TINYINT(1)")
  val Int8            = self.SqlType("TINYINT")
  val Int16           = self.SqlType("SMALLINT")
  val Int32           = self.SqlType("INT")
  val Int64           = self.SqlType("BIGINT")
  val UInt8           = self.SqlType("TINYINT  UNSIGNED")
  val UInt16          = self.SqlType("SMALLINT UNSIGNED")
  val UInt32          = self.SqlType("INT      UNSIGNED")
  val UInt64          = self.SqlType("BIGINT   UNSIGNED")
  val Utf8Char8       = self.SqlType("VARCHAR(8)   CHARACTER SET utf8")
  val Utf8Char16      = self.SqlType("VARCHAR(16)  CHARACTER SET utf8")
  val Utf8Char32      = self.SqlType("VARCHAR(32)  CHARACTER SET utf8")
  val Utf8Char64      = self.SqlType("VARCHAR(64)  CHARACTER SET utf8")
  val Utf8Char128     = self.SqlType("VARCHAR(128) CHARACTER SET utf8")
  val Utf8Char255     = self.SqlType("VARCHAR(255) CHARACTER SET utf8")
  val AsciiChar8      = self.SqlType("VARCHAR(8)   CHARACTER SET ascii")
  val AsciiChar16     = self.SqlType("VARCHAR(16)  CHARACTER SET ascii")
  val AsciiChar32     = self.SqlType("VARCHAR(32)  CHARACTER SET ascii")
  val AsciiChar64     = self.SqlType("VARCHAR(64)  CHARACTER SET ascii")
  val AsciiChar128    = self.SqlType("VARCHAR(128) CHARACTER SET ascii")
  val AsciiChar255    = self.SqlType("VARCHAR(255) CHARACTER SET ascii")
  val Utf8BinChar8    = self.SqlType("VARCHAR(8)   CHARACTER SET utf8  COLLATE utf8_bin")
  val Utf8BinChar16   = self.SqlType("VARCHAR(16)  CHARACTER SET utf8  COLLATE utf8_bin")
  val Utf8BinChar32   = self.SqlType("VARCHAR(32)  CHARACTER SET utf8  COLLATE utf8_bin")
  val Utf8BinChar64   = self.SqlType("VARCHAR(64)  CHARACTER SET utf8  COLLATE utf8_bin")
  val Utf8BinChar128  = self.SqlType("VARCHAR(128) CHARACTER SET utf8  COLLATE utf8_bin")
  val Utf8BinChar255  = self.SqlType("VARCHAR(255) CHARACTER SET utf8  COLLATE utf8_bin")
  val AsciiCharBin8   = self.SqlType("VARCHAR(8)   CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin16  = self.SqlType("VARCHAR(16)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin32  = self.SqlType("VARCHAR(32)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin64  = self.SqlType("VARCHAR(64)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin128 = self.SqlType("VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin255 = self.SqlType("VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin")
  val DateTime        = self.SqlType("DATETIME")
  val Date            = self.SqlType("DATE")
  val Ts              = self.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  val TsCurrent       = self.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  val Text            = self.SqlType("TEXT CHARACTER SET utf8")
  val Blob            = self.SqlType("BLOB")

  def Decimal(m: Int, d: Int) = self.SqlType(s"DECIMAL($m, $d)")
}

trait SlickColumnOptionOps {
  implicit def slickColumnOptionsExtension(a: P#ColumnOptions) =
    new SlickColumnOptionsExtension(a)
}
