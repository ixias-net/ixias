/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import java.time.{ LocalDate, YearMonth }
import play.api.mvc.QueryStringBindable

trait JavaTimeBindable {

  // --[ Typedefs ]-------------------------------------------------------------
  type LocalDate      =  java.time.LocalDate
  type LocalDateRange = (java.time.LocalDate, java.time.LocalDate)
  type YearMonth      =  java.time.YearMonth
  type YearMonthRange = (java.time.YearMonth, java.time.YearMonth)

  // -- [ LocalDate ] ----------------------------------------------------------
  /**
   * QueryString binder for LocalDate
   */
  implicit object queryStringBindableLocalDate extends QueryStringBindable.Parsing[LocalDate](
    (s: String)       => LocalDate.parse(s),
    (date: LocalDate) => date.toString,
    (key: String, e: Exception) => {
      "Cannot parse parameter %s as LocalDate: %s"
        .format(key, e.getMessage)
    }
  )

  /**
   * QueryString binder for LocalDate -> LocalDate
   */
  implicit object queryStringBindableLocalDateRange extends QueryStringBindable.Parsing[LocalDateRange](
    (s: String) => s.split(",").map(LocalDate.parse).toSeq match {
      case Seq(v1, v2) => (v1, v2)
      case _           => throw new IllegalArgumentException("The date-range value syntax is not valid")
    },
    (r: LocalDateRange) => Seq(r._1.toString, r._2.toString).mkString(","),
    (key: String, e: Exception) => {
      "Cannot parse parameter %s as LocalDateRange: %s"
        .format(key, e.getMessage)
    }
  )

  // -- [ YearMonth ] ----------------------------------------------------------
  /**
   * QueryString binder for YearMonth
   */
  implicit object queryStringBindableYearMonth extends QueryStringBindable.Parsing[YearMonth](
    (s: String)         => YearMonth.parse(s),
    (ymonth: YearMonth) => ymonth.toString,
    (key: String, e: Exception) => {
      "Cannot parse parameter %s as YearMonth: %s"
        .format(key, e.getMessage)
    }
  )

  /**
   * QueryString binder for YearMonth -> YearMonth
   */
  implicit object queryStringBindableYearMonthRange extends QueryStringBindable.Parsing[YearMonthRange](
    (s: String) => s.split(",").map(YearMonth.parse).toSeq match {
      case Seq(v1, v2) => (v1, v2)
      case _           => throw new IllegalArgumentException("The date-range value syntax is not valid")
    },
    (r: YearMonthRange) => Seq(r._1.toString, r._2.toString).mkString(","),
    (key: String, e: Exception) => {
      "Cannot parse parameter %s as YearMonthRange: %s"
        .format(key, e.getMessage)
    }
  )
}
