/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.lifted

import java.sql._
import java.util.Calendar
import org.joda.time._
import slick.driver.JdbcProfile
import slick.jdbc.{ SetParameter, PositionedParameters, GetResult, PositionedResult }
import scala.language.reflectiveCalls
import scala.language.implicitConversions

final case class SlickColumnTypesExtension[P <: JdbcProfile](val driver: P) {
  val jodaDateTime = new JodaDateTimeJdbc

  /** org.joda.time.DateTime */
  class JodaDateTimeJdbc {
    type T1 = DateTime
    type T2 = java.sql.Timestamp

    protected def toT1(v: T2): T1 = if (v == null) null else new DateTime(v.getTime)
    protected def toT2(v: T1): T2 = if (v == null) null else new Timestamp(v.getMillis)
    protected def toCalendar(v: T1): Calendar = Calendar.getInstance(v.getZone().toTimeZone())

    object Type extends driver.DriverJdbcType[T1] {
      def sqlType = java.sql.Types.TIMESTAMP
      def zero = new DateTime(0L)
      def getValue(r: ResultSet, idx: Int): T1 = toT1(r.getTimestamp(idx))
      def updateValue(v: T1, r: ResultSet, idx: Int): Unit = r.updateTimestamp(idx, toT2(v))
      def setValue(v: T1, p: PreparedStatement, idx: Int): Unit = p.setTimestamp(idx, toT2(v), toCalendar(v))
      override def valueToSQLLiteral(value: T1): String = "{ts '" + toT2(value).toString + "'}"
    }
    object Get    extends GetResult[T1]            { def apply(rs: PositionedResult) = toT1(rs.nextTimestamp()) }
    object GetOpt extends GetResult[Option[T1]]    { def apply(rs: PositionedResult) = rs.nextTimestampOption.map(toT1) }
    object Set    extends SetParameter[T1]         { def apply(v: T1,         pp: PositionedParameters) = pp.setTimestamp(toT2(v)) }
    object SetOpt extends SetParameter[Option[T1]] { def apply(v: Option[T1], pp: PositionedParameters) = pp.setTimestampOption(v.map(toT2)) }
  }
}

trait SlickColumnTypeOps[P <: JdbcProfile] {
  val driver: P
  val columnTypes = SlickColumnTypesExtension(driver)
  implicit val jodaDateTimeColumnType      = columnTypes.jodaDateTime.Type
  implicit val jodaDateTimeGetResult       = columnTypes.jodaDateTime.Get
  implicit val jodaDateTimeGetResultOpt    = columnTypes.jodaDateTime.GetOpt
  implicit val jodaDateTimeSetParameter    = columnTypes.jodaDateTime.Set
  implicit val jodaDateTimeSetParameterOpt = columnTypes.jodaDateTime.SetOpt
}
