/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.language.implicitConversions

/** Backend for the basic database and session handling features. */
trait Backend extends DataSource

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
