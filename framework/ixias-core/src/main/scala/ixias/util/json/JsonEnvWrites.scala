/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util.json

import play.api.libs.json._
import play.api.libs.json.EnvWrites

/**
 * Writes Conbinator for type conversion in service
 */
trait JsonEnvWrites extends EnvWrites {

  /**
   * Serializer forshapeless.tag.@@[Long, _]
   */
  implicit def TagLongWrites[T] =
    new Writes[shapeless.tag.@@[Long, T]] {
      def writes(tag: shapeless.tag.@@[Long, T]) = JsNumber(tag)
    }

  /**
   * Serializer forshapeless.tag.@@[String, _]
   */
  implicit def TagStringWrites[T] =
    new Writes[shapeless.tag.@@[String, T]] {
      def writes(tag: shapeless.tag.@@[String, T]) = JsString(tag)
    }

  /**
   * Serializer for ixias.util.EnumStatus
   */
  implicit def EnumStatusWrites[T <: ixias.util.EnumStatus] =
    new Writes[T] {
      def writes(v: T) = JsNumber(v.code)
    }

  /**
   * Serializer for ixias.util.EnumStatusAsstr
   */
  implicit def EnumStatusAsStrWrites[T <: ixias.util.EnumStatusAsStr] =
    new Writes[T] {
      def writes(v: T) = JsString(v.code)
    }

  /**
   * Serializer for Seq[ixias.util.EnumBitFlags]
   */
  implicit def EnumBitFlagsWrites[T <: ixias.util.EnumBitFlags] =
    new Writes[T] {
      def writes(v: T) = JsNumber(v.code)
    }

  /**
   * Serializer for ixias.persistence.model.Cursor
   */
  implicit object CursorWrites extends Writes[ixias.persistence.model.Cursor] {
    def writes(cursor: ixias.persistence.model.Cursor) =
      JsObject(Seq(
        Some(            "offset" -> JsNumber(cursor.offset)),
        cursor.limit.map("limit"  -> JsNumber(_))
      ).flatten)
  }

  /**
   * Serializer for java.time.YearMonth
   */
  implicit object YearMonthWrites extends Writes[java.time.YearMonth] {
    def writes(yearMonth: java.time.YearMonth) =
      JsObject(Seq(
        "year"  -> JsNumber(yearMonth.getYear),
        "month" -> JsNumber(yearMonth.getMonthValue),
        "text"  -> JsString(yearMonth.format(
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        ))
      ))
  }
}
