/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.jdbc

import java.sql.{ PreparedStatement, ResultSet, Time, Timestamp }
import java.time.{ LocalDateTime, LocalTime }

/**
 * Slick's MySQL profile with the java.time column types mapped back onto real
 * temporal SQL types.
 *
 * Slick 3.3 started persisting LocalDateTime as VARCHAR holding an ISO-8601
 * string, and reads it with LocalDateTime.parse(rs.getString(..)). Against an
 * actual DATETIME column MySQL answers "2026-07-10 09:52:40" -- a space, not a
 * 'T' -- so every read throws DateTimeParseException, and writes push an ISO
 * string into a DATETIME. LocalTime has the same VARCHAR treatment in Slick's
 * base profile.
 *
 * Slick 3.2 had no profile-level implementation, so ixias' MappedColumnType in
 * SlickColumnTypeOps was the only candidate and won. From 3.3 the profile ships
 * one typed as the concrete *JdbcType, which is more specific than
 * BaseColumnType[T], so it silently outranks the mapping instead of clashing
 * with it. Overriding the profile is therefore the only place the behaviour can
 * be restored for good.
 *
 * Use this in place of slick.jdbc.MySQLProfile.
 */
trait MySQLProfile extends slick.jdbc.MySQLProfile {

  override val columnTypes = new JdbcTypes {

    override val localDateTimeType: LocalDateTimeJdbcType = new LocalDateTimeJdbcType {
      override def sqlType: Int = java.sql.Types.TIMESTAMP
      override def setValue(v: LocalDateTime, p: PreparedStatement, idx: Int): Unit =
        p.setTimestamp(idx, if (v == null) null else Timestamp.valueOf(v))
      override def getValue(r: ResultSet, idx: Int): LocalDateTime =
        r.getTimestamp(idx) match {
          case null => null
          case ts   => ts.toLocalDateTime
        }
      override def updateValue(v: LocalDateTime, r: ResultSet, idx: Int): Unit =
        r.updateTimestamp(idx, if (v == null) null else Timestamp.valueOf(v))
      override def valueToSQLLiteral(value: LocalDateTime): String =
        if (value == null) "NULL" else s"'${Timestamp.valueOf(value).toString}'"
    }

    override val localTimeType: LocalTimeJdbcType = new LocalTimeJdbcType {
      override def sqlType: Int = java.sql.Types.TIME
      override def setValue(v: LocalTime, p: PreparedStatement, idx: Int): Unit =
        p.setTime(idx, if (v == null) null else Time.valueOf(v))
      override def getValue(r: ResultSet, idx: Int): LocalTime =
        r.getTime(idx) match {
          case null => null
          case t    => t.toLocalTime
        }
      override def updateValue(v: LocalTime, r: ResultSet, idx: Int): Unit =
        r.updateTime(idx, if (v == null) null else Time.valueOf(v))
      override def valueToSQLLiteral(value: LocalTime): String =
        if (value == null) "NULL" else s"'${Time.valueOf(value).toString}'"
    }
  }
}

object MySQLProfile extends MySQLProfile
