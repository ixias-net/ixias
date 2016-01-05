/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import scala.util.{ Try, Success, Failure }
import scala.language.implicitConversions

private[config] trait DataSourceConfig {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The DSN structure. */
  case class DataSourceName(
    val path:     String,
    val hostspec: String,
    val database: String
  )

  // --[ Methods ]--------------------------------------------------------------
  protected implicit def convertToStruct(name: String): Try[DataSourceName] = {
    val syntax = """^([.\w]+)://(\w+?)/(\w+)$""".r
    val struct = name match {
      case syntax(p1, p2, p3) => DataSourceName(p1, p2, p3)
      case _ => throw new Exception(s"""Dose not match the DSN format. ($name)""")
    }
    Success(struct)
  }
}
