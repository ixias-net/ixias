/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.jdbc.JdbcProfile

trait SlickColumnTypeOps[P <: JdbcProfile] {
  val driver: P
  import driver.api._

  // --[ Java8 Time ]-----------------------------------------------------------
  // java.sql.Timestamp <-> java.time.LocalDateTime
  implicit val javaLocalDateTimeColumnType =
    MappedColumnType.base[java.time.LocalDateTime, java.sql.Timestamp](
      dt => java.sql.Timestamp.valueOf(dt),
      ts => ts.toLocalDateTime()
    )

  // java.sql.Date <-> java.time.LocalDate
  implicit val javaLocalDateColumnType =
    MappedColumnType.base[java.time.LocalDate, java.sql.Date](
      ld => java.sql.Date.valueOf(ld),
      d  => d.toLocalDate()
    )

  // java.sql.Time <-> java.time.LocalTime
  implicit val javaLocalTimeColumnType =
    MappedColumnType.base[java.time.LocalTime, java.sql.Time](
      lt => java.sql.Time.valueOf(lt),
      t  => t.toLocalTime()
    )

  // --[ Joda Time ]------------------------------------------------------------
  // java.sql.Timestamp <-> org.joda.time.DateTime
  implicit val jodaDateTimeColumnType =
    MappedColumnType.base[org.joda.time.DateTime, java.sql.Timestamp](
      dt => new java.sql.Timestamp(dt.getMillis),
      ts => new org.joda.time.DateTime(ts.getTime)
    )

  // java.sql.Date <-> org.joda.time.LocalDate
  implicit val jodaLocalDateColumnType =
    MappedColumnType.base[org.joda.time.LocalDate, java.sql.Date](
      ld => new java.sql.Date(ld.toDateTimeAtStartOfDay(org.joda.time.DateTimeZone.UTC).getMillis),
      d  => new org.joda.time.LocalDate(d.getTime)
    )

  // java.sql.Time <-> org.joda.time.LocalTime
  implicit val jodaLocalTimeColumnType =
    MappedColumnType.base[org.joda.time.LocalTime, java.sql.Time](
      lt => new java.sql.Time(lt.toDateTimeToday.getMillis),
      t  => new org.joda.time.LocalTime(t, org.joda.time.DateTimeZone.UTC)
    )
}
