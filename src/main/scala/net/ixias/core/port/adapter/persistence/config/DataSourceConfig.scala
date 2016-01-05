/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import scala.util.Try
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import com.typesafe.config.Config
import core.port.adapter.persistence.io.EntityIOActionContext

trait DataSourceConfig { self: DataSource =>

  /** The keys of configuration */
  protected val CF_HOSTSPEC_HOSTS   = """hostspec.%s.hosts"""
  protected val CY_DATA_SOURCE_NAME = """^([.\w]+)://(\w+?)/(\w+)$""".r

  // --[ Methods ]--------------------------------------------------------------
  protected def getHosts(dsn: DataSourceName)(implicit ctx: Context): Try[List[String]] = {
    val path = CF_HOSTSPEC_HOSTS.format(dsn.hostspec)
    getAnyRef(dsn, path).map[List[String]](_ match {
      case v: String => List(v)
      case v: java.util.List[_] => v.toList.asInstanceOf[List[String]]
      case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
    })
  }

  // --[ DataSouceName ]--------------------------------------------------------
  /** The DSN structure. */
  case class DataSourceName(
    val path:     String,
    val hostspec: String,
    val database: String
  )

  /** Convert to structure data from dsn as string */
  protected implicit def convertToStruct(name: String): DataSourceName =
    name match {
      case CY_DATA_SOURCE_NAME(p1, p2, p3) => DataSourceName(p1, p2, p3)
      case _ => throw new Exception(s"""Dose not match the DSN format. ($name)""")
    }

  // --[ Configuration ]--------------------------------------------------------
  /** Get a typesafe config accessor */
  protected def withConfig[A](f: (Config) => A)(implicit ctx: Context) = f(ctx.config)

  /** Get a configuration value. */
  final protected def getAnyRef(dsn: DataSourceName, path: String)(implicit ctx: Context): Try[AnyRef] =
    withConfig { cfg =>
      val root    = cfg.getConfig(dsn.path)
      val section = root.getConfig(dsn.database)
      Try(section.getAnyRef(path)).recover { case _ =>
        Try(root.getAnyRef(path))
      }
    }
}
