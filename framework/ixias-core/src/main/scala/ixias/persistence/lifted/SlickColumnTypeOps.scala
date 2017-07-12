/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.driver.JdbcProfile
import java.sql.{ Timestamp, Date, Time }
import org.joda.time.{ DateTime, LocalDate, LocalTime, Duration, DateTimeZone }
import ixias.model.Identity

trait SlickColumnTypeOps[P <: JdbcProfile] {
  val driver: P
  import driver.api._

  // java.sql.Timestamp <-> org.joda.time.DateTime
  implicit val jodaDateTimeColumnType = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis),
    ts => new DateTime(ts.getTime)
  )

  // java.sql.Date <-> org.joda.time.LocalDate
  implicit val jodaLocalDateColumnType = MappedColumnType.base[LocalDate, Date](
    ld => new Date(ld.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis),
    d  => new LocalDate(d.getTime)
  )

  // java.sql.Time <-> org.joda.time.LocalTime
  implicit val jodaLocalTimeColumnType = MappedColumnType.base[LocalTime, Time](
    lt => new Time(lt.toDateTimeToday.getMillis),
    t  => new LocalTime(t)
  )

  // java.sql.Time <-> org.joda.time.Duration
  implicit val jodaDurationColumnType = MappedColumnType.base[Duration, Time](
    d => new Time(d.getMillis),
    t => new Duration(t)
  )
}
