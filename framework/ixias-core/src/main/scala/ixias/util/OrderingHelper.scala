/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.math.Ordering

trait OrderingHelper {

  // --[ For Option ]-----------------------------------------------------------
  def noneFirstAsc  [T](implicit o: Ordering[T]): Ordering[Option[T]] = Ordering.Option
  def noneFirstDesc [T](implicit o: Ordering[T]): Ordering[Option[T]] = noneFirstAsc(o.reverse)
  def noneLastDesc  [T](implicit o: Ordering[T]): Ordering[Option[T]] = noneFirstAsc(o).reverse
  def noneLastAsc   [T](implicit o: Ordering[T]): Ordering[Option[T]] = noneLastDesc(o.reverse)

  // --[ Ordering ]-------------------------------------------------------------
  /**
   * For Japanses String
   */
  implicit val StringJP: Ordering[String] = Ordering.comparatorToOrdering(
    java.text.Collator.getInstance(java.util.Locale.JAPANESE)
      .asInstanceOf[java.util.Comparator[String]]
  )

  /**
   * For java.time.YearMonth
   */
  trait YearMonthOrdering extends Ordering[java.time.YearMonth] {
    def compare(x: java.time.YearMonth, y: java.time.YearMonth): Int = x compareTo y
  }
  implicit object YearMonth extends YearMonthOrdering

  /**
   * For java.time.LocalDate
   */
  trait LocalDateOrdering extends Ordering[java.time.LocalDate] {
    def compare(x: java.time.LocalDate, y: java.time.LocalDate): Int = x compareTo y
  }
  implicit object LocalDate extends LocalDateOrdering

  /**
   * For java.time.LocalDateTime
   */
  trait LocalDateTimeOrdering extends Ordering[java.time.LocalDateTime] {
    def compare(x: java.time.LocalDateTime, y: java.time.LocalDateTime): Int = x compareTo y
  }
  implicit object LocalDateTime extends LocalDateTimeOrdering

  /**
   * For java.time.Duration
   */
  trait DurationOrdering extends Ordering[java.time.Duration] {
    def compare(x: java.time.Duration, y: java.time.Duration): Int = x compareTo y
  }
  implicit object Duration extends DurationOrdering
}
