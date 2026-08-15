/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import java.time.{ YearMonth, LocalDate, Instant, Clock, ZoneOffset }
import java.time.temporal.UnsupportedTemporalTypeException
import java.time.format.{ DateTimeFormatter, DateTimeParseException }
import play.api.libs.json._
import scala.language.implicitConversions

object YearMonthReads extends EnvReads {

  /** Instance of year-month based on formatter. */
  implicit def YearMonthFormatterParser(formatter: DateTimeFormatter): TemporalParser[YearMonth] =
    new TemporalParser[YearMonth] {
      def parse(input: String): Option[YearMonth] = try {
        Some(YearMonth.parse(input, formatter))
      } catch {
        case _: DateTimeParseException => None
        case _: UnsupportedTemporalTypeException => None
      }
    }

  /**
   * Reads for the `java.time.YearMonth` type.
   */
  def yearMonthReads[T](parsing: T, corrector: String => String = identity)(implicit p: T => TemporalParser[YearMonth]): Reads[YearMonth] =
    new Reads[YearMonth] {
      def reads(json: JsValue): JsResult[YearMonth] = json match {
        case JsNumber(d) => JsSuccess(YearMonth.from(epoch(d.toLong)))
        case JsString(s) => p(parsing).parse(corrector(s)) match {
          case Some(d) => JsSuccess(d)
          case _ => JsError(Seq(JsPath ->
            Seq(JsonValidationError("error.expected.date.isoformat", parsing))))
        }
        case _ => JsError(Seq(JsPath ->
          Seq(JsonValidationError("error.expected.date"))))
      }

      @inline def epoch(millis: Long): LocalDate = LocalDate.now(
        Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
      )
    }

  /**
   * The default typeclass to reads `java.time.YearMonth` from JSON.
   */
  implicit val DefaultYearMonthReads: Reads[YearMonth] =
    yearMonthReads(DateTimeFormatter.ofPattern("yyyy-MM"))
}
