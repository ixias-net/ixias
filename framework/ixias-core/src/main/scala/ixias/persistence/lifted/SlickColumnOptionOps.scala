/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.sql.SqlProfile.ColumnOption.SqlType

final class SlickColumnOptionsExtension {
  val Boolean         = SqlType("BIT(1)")
  val Int8            = SqlType("TINYINT")
  val Int16           = SqlType("SMALLINT")
  val Int32           = SqlType("INT")
  val Int64           = SqlType("BIGINT")
  val UInt8           = SqlType("TINYINT  UNSIGNED")
  val UInt16          = SqlType("SMALLINT UNSIGNED")
  val UInt32          = SqlType("INT      UNSIGNED")
  val UInt64          = SqlType("BIGINT   UNSIGNED")
  val Utf8Char8       = SqlType("VARCHAR(8)   CHARACTER SET utf8mb4")
  val Utf8Char16      = SqlType("VARCHAR(16)  CHARACTER SET utf8mb4")
  val Utf8Char32      = SqlType("VARCHAR(32)  CHARACTER SET utf8mb4")
  val Utf8Char64      = SqlType("VARCHAR(64)  CHARACTER SET utf8mb4")
  val Utf8Char128     = SqlType("VARCHAR(128) CHARACTER SET utf8mb4")
  val Utf8Char255     = SqlType("VARCHAR(255) CHARACTER SET utf8mb4")
  val AsciiChar8      = SqlType("VARCHAR(8)   CHARACTER SET ascii")
  val AsciiChar16     = SqlType("VARCHAR(16)  CHARACTER SET ascii")
  val AsciiChar32     = SqlType("VARCHAR(32)  CHARACTER SET ascii")
  val AsciiChar64     = SqlType("VARCHAR(64)  CHARACTER SET ascii")
  val AsciiChar128    = SqlType("VARCHAR(128) CHARACTER SET ascii")
  val AsciiChar255    = SqlType("VARCHAR(255) CHARACTER SET ascii")
  val Utf8BinChar8    = SqlType("VARCHAR(8)   CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val Utf8BinChar16   = SqlType("VARCHAR(16)  CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val Utf8BinChar32   = SqlType("VARCHAR(32)  CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val Utf8BinChar64   = SqlType("VARCHAR(64)  CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val Utf8BinChar128  = SqlType("VARCHAR(128) CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val Utf8BinChar255  = SqlType("VARCHAR(255) CHARACTER SET utf8  COLLATE utf8mb4_bin")
  val AsciiCharBin8   = SqlType("VARCHAR(8)   CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin16  = SqlType("VARCHAR(16)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin32  = SqlType("VARCHAR(32)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin64  = SqlType("VARCHAR(64)  CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin128 = SqlType("VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin")
  val AsciiCharBin255 = SqlType("VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin")
  val DateTime        = SqlType("DATETIME")
  val Date            = SqlType("DATE")
  val Time            = SqlType("TIME")
  val Ts              = SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  val TsCurrent       = SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  val Text            = SqlType("TEXT CHARACTER SET utf8")
  val Blob            = SqlType("BLOB")
  def Decimal(m: Int, d: Int) = SqlType(s"DECIMAL($m, $d)")
}

trait SlickColumnOptionOps {
  implicit def slickColumnOptionsExtension = new SlickColumnOptionsExtension
}
