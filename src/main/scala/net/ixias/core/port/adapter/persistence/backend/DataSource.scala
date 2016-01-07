/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.Try
import scala.language.implicitConversions
import core.port.adapter.persistence.io.EntityIOActionContext

trait DataSource {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DataSource <: DataSourceDef

  /** The type of the database souce config factory used by this backend. */
  type DataSourceFactory <: DataSourceFactoryDef

  /** The type of the context used for running Database Actions */
  type Context >: Null <: EntityIOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DataSource: DataSourceFactory

  // --[ Traits ]---------------------------------------------------------------
  /** A database souce config instance to which connections can be created. */
  trait DataSourceDef

  /** The factory to create a database source config. */
  trait DataSourceFactoryDef {
    /** Load a configuration for persistent database. */
    def forDSN(name: String)(implicit ctx: Context): Try[DataSource]
  }
}

/** The DSN structure. */
case class DataSourceName(
  val path:     String,
  val hostspec: String,
  val database: String
)

/** Conpanion object */
object DataSourceName {

  /** The synatx format for DSN */
  val SYNTAX_DATA_SOURCE_NAME = """^([.\w]+)://(\w+?)/(\w+)$""".r

  object Implicits {
    /** Convert to structure data from dsn as string */
    implicit def convertToStruct(name: String): DataSourceName = name match {
      case SYNTAX_DATA_SOURCE_NAME(p1, p2, p3) => DataSourceName(p1, p2, p3)
      case _ => throw new Exception(s"""Dose not match the DSN format. ($name)""")
    }
  }
}
