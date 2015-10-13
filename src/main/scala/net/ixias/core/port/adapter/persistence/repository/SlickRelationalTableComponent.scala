/*
 *  This file is part of the nextbeat services.
 *
 *  For the full copyright and license information,
 *  please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.repository

import slick.profile.SqlProfile
import scala.language.implicitConversions

trait SlickRelationalTableComponent[P <: SqlProfile] {

  // --[ Properties ]-----------------------------------------------------------
  /** The type of the relation table column's option. */
  type ColumnOption = P#ColumnOptions

  // --[ ColumnOptions ]--------------------------------------------------------
  class ColumnOptionsExtension(option: ColumnOption) {
    val Int8            = option.SqlType("TINYINT")
    val Int16           = option.SqlType("SMALLINT")
    val Int32           = option.SqlType("INT")
    val Int64           = option.SqlType("BIGINT")
    val UInt8           = option.SqlType("TINYINT  UNSIGNED")
    val UInt16          = option.SqlType("SMALLINT UNSIGNED")
    val UInt32          = option.SqlType("INT      UNSIGNED")
    val UInt64          = option.SqlType("BIGINT   UNSIGNED")
    val Utf8Char8       = option.SqlType("VARCHAR(8)   CHARACTER SET utf8")
    val Utf8Char16      = option.SqlType("VARCHAR(16)  CHARACTER SET utf8")
    val Utf8Char32      = option.SqlType("VARCHAR(32)  CHARACTER SET utf8")
    val Utf8Char64      = option.SqlType("VARCHAR(64)  CHARACTER SET utf8")
    val Utf8Char128     = option.SqlType("VARCHAR(128) CHARACTER SET utf8")
    val Utf8Char255     = option.SqlType("VARCHAR(255) CHARACTER SET utf8")
    val AsciiChar8      = option.SqlType("VARCHAR(8)   CHARACTER SET ascii")
    val AsciiChar16     = option.SqlType("VARCHAR(16)  CHARACTER SET ascii")
    val AsciiChar32     = option.SqlType("VARCHAR(32)  CHARACTER SET ascii")
    val AsciiChar64     = option.SqlType("VARCHAR(64)  CHARACTER SET ascii")
    val AsciiChar128    = option.SqlType("VARCHAR(128) CHARACTER SET ascii")
    val AsciiChar255    = option.SqlType("VARCHAR(255) CHARACTER SET ascii")
    val Utf8BinChar8    = option.SqlType("VARCHAR(8)   CHARACTER SET utf8  COLLATE utf8_bin")
    val Utf8BinChar16   = option.SqlType("VARCHAR(16)  CHARACTER SET utf8  COLLATE utf8_bin")
    val Utf8BinChar32   = option.SqlType("VARCHAR(32)  CHARACTER SET utf8  COLLATE utf8_bin")
    val Utf8BinChar64   = option.SqlType("VARCHAR(64)  CHARACTER SET utf8  COLLATE utf8_bin")
    val Utf8BinChar128  = option.SqlType("VARCHAR(128) CHARACTER SET utf8  COLLATE utf8_bin")
    val Utf8BinChar255  = option.SqlType("VARCHAR(255) CHARACTER SET utf8  COLLATE utf8_bin")
    val AsciiCharBin8   = option.SqlType("VARCHAR(8)   CHARACTER SET ascii COLLATE ascii_bin")
    val AsciiCharBin16  = option.SqlType("VARCHAR(16)  CHARACTER SET ascii COLLATE ascii_bin")
    val AsciiCharBin32  = option.SqlType("VARCHAR(32)  CHARACTER SET ascii COLLATE ascii_bin")
    val AsciiCharBin64  = option.SqlType("VARCHAR(64)  CHARACTER SET ascii COLLATE ascii_bin")
    val AsciiCharBin128 = option.SqlType("VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin")
    val AsciiCharBin255 = option.SqlType("VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin")
    val Ts              = option.SqlType("TIMESTAMP")
    val TsCurrent       = option.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    val TEXT            = option.SqlType("TEXT CHARACTER SET utf8")
  }

  // --[ Implicit Methods ]-----------------------------------------------------
  trait ImplicitColumnOptions {
    @inline implicit def columnOptionsExtension(option: ColumnOption) =
      new ColumnOptionsExtension(option)
  }
}
