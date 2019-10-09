/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

import ixias.persistence.model.DataSourceName

/**
 * The model of AmazonQLDB table.
 */
trait Table[T] {

  //-- [ Properties ] ----------------------------------------------------------
  /** Data storage location information */
  val dsn:   DataSourceName

  /** Table queries */
  val query: TableQuery

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * Overwrite function if necessary.
   * As you add and change features in your app,
   * you need to modify your entity classes to reflect these adjust.
   */
  def migrate[A](data: A): A = data
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
}


// For Test
//~~~~~~~~~~
object TestTable extends Table[Long] {
  import software.amazon.qldb.QldbSession
  import ixias.aws.qldb.dbio.Database

  val dsn  = DataSourceName("")
  val test = (session: QldbSession) => {
    val db = Database(session)
    db.execute(tx => {
      tx.execute(query.find1(1l))
    })
    query
  }
  object query extends TableQuery("hogehoge") {
    def find1(sid: Long)              = sql("SELECT __TABLE_NAME__ WHERE sid = ?", sid)
    def find2(sid: Long, oid: String) = sql("SELECT __TABLE_NAME__ WHERE sid = ? AND oid = ?", sid, oid)
  }
}
