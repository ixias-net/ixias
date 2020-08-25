/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util.json

import play.api.libs.json._
import play.api.libs.json.EnvReads
import scala.util.{ Try, Success, Failure }

/**
 * Reads Conbinator for type conversion in service
 */
trait JsonEnvReads extends EnvReads {

  /**
   * Deserializer for ixias.model.@@[Long, _]
   */
  def idAsNumberReads[T <: ixias.model.@@[Long, _]]: Reads[T] =
    new Reads[T] {
      def reads(json: JsValue) = json match {
        case JsNumber(n) if n.isValidLong => {
          val Id = ixias.model.the[ixias.model.Identity[T]]
          JsSuccess(Id(n.toLong.asInstanceOf[T]))
        }
        case JsNumber(n) => JsError("error.expected.tag.long")
        case _           => JsError("error.expected.tag.jsnumber")
      }
    }

  /**
   * Deserializer for ixias.model.@@[String, _]
   */
  def idAsStrReads[T <: ixias.model.@@[String, _]]: Reads[T] =
    new Reads[T] {
      def reads(json: JsValue) = json match {
        case JsString(s) => {
          val Id = ixias.model.the[ixias.model.Identity[T]]
          JsSuccess(Id(s.asInstanceOf[T]))
        }
        case _  => JsError("error.expected.tag.jsstring")
      }
    }

  /**
   * Deserializer for ixias.util.EnumStatus
   */
  def enumReads[E <: ixias.util.EnumStatus](enum: ixias.util.EnumStatus.Of[E]): Reads[E] =
    new Reads[E] {
      def reads(json: JsValue) = json match {
        case JsNumber(n) if n.isValidShort => JsSuccess(enum(n.toShort))
        case JsNumber(n) => JsError("error.expected.enum.short")
        case _           => JsError("error.expected.enum.jsnumber")
      }
    }

  /**
   * Deserializer for ixias.util.EnumBitFlags
   */
  def enumReads[E <: ixias.util.EnumBitFlags](enum: ixias.util.EnumBitFlags.Of[E]): Reads[Seq[E]] =
    new Reads[Seq[E]] {
      def reads(json: JsValue) = json match {
        case JsNumber(n) if n.isValidLong => JsSuccess(enum(n.toLong))
        case JsNumber(n) => JsError("error.expected.enum.long")
        case _           => JsError("error.expected.enum.jsnumber")
      }
    }

  /**
   * Deserializer for java.time.YearMonth
   */
  implicit object YearMonthReads extends Reads[java.time.YearMonth] {
    def reads(json: JsValue) = json match {
      case JsNumber(millis) =>
        JsSuccess(java.time.YearMonth.now(
          java.time.Clock.fixed(
            java.time.Instant.ofEpochMilli(millis.toLong),
            java.time.ZoneOffset.UTC
          )
        ))
      case JsString(s) => {
        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        Try(java.time.YearMonth.parse(s, fmt)) match {
          case Success(v) => JsSuccess(v)
          case Failure(_) => JsError(JsonValidationError("error.expected.date.format", fmt))
        }
      }
      case _ => for {
        v1 <- (json \ "year" ).validate[Int]
        v2 <- (json \ "month").validate[Int]
      } yield java.time.YearMonth.of(v1, v2)
    }
  }
}
