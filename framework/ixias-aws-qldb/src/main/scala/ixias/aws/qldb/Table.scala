/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import scala.language.implicitConversions

import com.amazon.ion.IonValue
import software.amazon.qldb.{ QldbSession, TransactionExecutor }
import ixias.persistence.model.DataSourceName

/**
 * The model of AmazonQLDB table.
 */
import Table._
trait  Table[T] {

  //-- [ Properties ] ----------------------------------------------------------
  /** Data storage location information */
  val dsn:   DataSourceName

  /** Table queries */
  val query: QldbSession => TableQuery

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * Overwrite function if necessary.
   * As you add and change features in your app,
   * you need to modify your entity classes to reflect these adjust.
   */
  def migrate[A](data: A): A = data
}

/**
 * Database
 */
case class Database(session: QldbSession) {
  import collection.JavaConverters._

  /** Execute query */
  def execute(stmt: SqlStatement) =
    session.execute(stmt.query, stmt.params.asJava)

  /** Execute query with transaction */
  def execute(stmt: SqlStatement)(implicit tx: TransactionExecutor) =
    tx.execute(stmt.query, stmt.params.asJava)

  /** Transaction block */
  def execute[A](block: TransactionExecutor => A): A =
    session.execute(block(_))
}

/**
 * Query statement definition
 */
case class SqlStatement(
  query:  String,
  params: Seq[IonValue]
)

/**
 * Definition to execute a query from the target table
 */
abstract class TableQuery(val tableName: String) {

  //-- [ Methods ] -------------------------------------------------------------
  /**
   * SQL query string validation and table name replacement
   */
  protected def buildQuery(stmt: SqlStatement): String =
    stmt.query.replaceFirst("__TABLE_NAME__", tableName)

  //-- [ Methods ] -------------------------------------------------------------
  /** Methods to create statement object */
  def sql[P1]
    (stmt: String, p1: P1)
      = SqlStatement(stmt, Seq(p1))
  def sql[P1, P2]
    (stmt: String, p1: P1, p2: P2)
      = SqlStatement(stmt, Seq(p1, p2))
  def sql[P1, P2, P3]
    (stmt: String, p1: P1, p2: P2, p3: P3)
      = SqlStatement(stmt, Seq(p1, p2, p3))
  def sql[P1, P2, P3, P4]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4))
  def sql[P1, P2, P3, P4, P5]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4, p5))
  def sql[P1, P2, P3, P4, P5, P6]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4, p5, p6))
  def sql[P1, P2, P3, P4, P5, P6, P7]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4, p5, p6, p7))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8))
  def sql[P1, P2, P3, P4, P5, P6, P7, P8, P9]
    (stmt: String, p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9)
      = SqlStatement(stmt, Seq(p1, p2, p3, p4, p5, p6, p7, p8, p9))
}


// Companion object
//~~~~~~~~~~~~~~~~~~
object Table {
  import com.fasterxml.jackson.dataformat.ion.IonObjectMapper
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
  import com.fasterxml.jackson.databind.SerializationFeature
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  //-- [ Properties ] ----------------------------------------------------------
  /**
   * Mapper for Ion object.
   */
  lazy val MAPPER_FOR_ION = {
    val mapper = new IonObjectMapper()
    mapper.registerModule(new JavaTimeModule)
      .registerModule(DefaultScalaModule)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper
  }

  // --[ Conv: For-Write ]------------------------------------------------------
  /**
   * Implicit converter: model data -> IonValue row data.
   */
  implicit def convToIonValue[A](v: A): IonValue = MAPPER_FOR_ION.writeValueAsIonValue(v)
}

// For Test
//~~~~~~~~~~
object ATable extends Table[Long] {

  val dsn   = DataSourceName("")
  val query = (session: QldbSession) => {
    val table = new TableQuery("hogehoge") {
      def find(sid: Long)              = sql("SELECT __TABLE_NAME__ WHERE sid = ?", sid)
      def find(sid: Long, oid: String) = sql("SELECT __TABLE_NAME__ WHERE sid = ? AND oid = ?", sid, oid)
    }
    table
  }
}
