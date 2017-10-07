/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.driver.JdbcProfile
import java.util.TimeZone
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
    lt => new Time(lt.toDateTimeToday.getMillis - TimeZone.getDefault.getRawOffset),
    t  => new LocalTime(t.getTime, DateTimeZone.UTC)
  )

  // java.sql.Time <-> org.joda.time.Duration
  implicit val jodaDurationColumnType =
    new driver.MappedJdbcType[Duration, String] with slick.ast.BaseTypedType[Duration] {
      override def sqlType = java.sql.Types.VARCHAR
      override def valueToSQLLiteral(d: Duration) = "{ ts '" + map(d) + "' }"
      override def getValue(r: java.sql.ResultSet, idx: Int) = {
        val v = r.getTimestamp(idx)
        (v.asInstanceOf[AnyRef] eq null) || tmd.wasNull(r, idx) match {
          case true  => null.asInstanceOf[Duration]
          case false => new Duration(v.getTime + TimeZone.getDefault.getRawOffset)
        }
      }
      def comap(str: String) = {
        val millis = LocalTime.parse(str).getMillisOfSecond.toLong
        new Duration(millis + TimeZone.getDefault.getRawOffset)
      }
      def map(d: Duration) = "%02d:%02d:%02d".format(
        d.getStandardHours,
        d.getStandardMinutes % 60,
        d.getStandardSeconds % 60
      )
    }
}
