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
import core.util.ConfigExt._
import core.port.adapter.persistence.io.EntityIOActionContext

trait DataSourceConfig { self: DataSource =>

  /** The synatx format for DSN */
  protected val CY_DATA_SOURCE_NAME       = """^([.\w]+)://(\w+?)/(\w+)$""".r

  /** The section format */
  protected val CF_SECTION_HOSTSPEC       = """hostspec.%s"""

  /** The keys of configuration (1) */
  protected val CF_USER                   = """user"""
  protected val CF_PASSWORD               = """password"""
  protected val CF_DRIVER_CLASS_NAME      = """driver_class_name"""

  /** The keys of configuration (2) */
  protected val CF_HOSTSPEC_HOSTS         = """hosts"""
  protected val CF_HOSTSPEC_READONLY      = """readonly"""
  protected val CF_HOSTSPEC_MIN_IDLE      = """min_idle"""
  protected val CF_HOSTSPEC_MAX_POOL_SIZE = """max_pool_size"""

  // --[ DataSouceName ]--------------------------------------------------------
  /** The DSN structure. */
  case class DataSourceName(
    val path:     String,
    val hostspec: String,
    val database: String
  )

  /** Convert to structure data from dsn as string */
  protected implicit def convertToStruct(name: String): DataSourceName = name match {
    case CY_DATA_SOURCE_NAME(p1, p2, p3) => DataSourceName(p1, p2, p3)
    case _ => throw new Exception(s"""Dose not match the DSN format. ($name)""")
  }

  // --[ Methods ]--------------------------------------------------------------
  protected def getUser(dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_USER))

  protected def getPassword(dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_PASSWORD))

  protected def getDriverClassName(dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_DRIVER_CLASS_NAME))

  // --[ Methods ]--------------------------------------------------------------
  protected def getHosts(dsn: DataSourceName)(implicit ctx: Context): Try[List[String]] =
    withConfig { cfg =>
      val root     = cfg.getConfig(dsn.path)
      val section  = root.getConfig(dsn.database)
      val hostspec = CF_SECTION_HOSTSPEC.format(dsn.hostspec)
      Try(section.getConfig(hostspec).getAnyRef(CF_HOSTSPEC_HOSTS)).map(_ match {
        case v: String => List(v)
        case v: java.util.List[_] => v.toList.asInstanceOf[List[String]]
        case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
      })
    }

  protected def getHostSpecReadOnly(dsn: DataSourceName)(implicit ctx: Context): Option[Boolean] =
    getOptionalHostSpecValue(dsn)(_.getBooleanOpt(CF_HOSTSPEC_READONLY))

  protected def getHostSpecMinIdle(dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalHostSpecValue(dsn)( _.getIntOpt(CF_HOSTSPEC_MIN_IDLE))

  protected def getHostSpecMaxPoolSize(dsn: DataSourceName)(implicit ctx: Context): Option[Int] =
    getOptionalHostSpecValue(dsn)(_.getIntOpt(CF_HOSTSPEC_MAX_POOL_SIZE))

  // --[ Configuration ]--------------------------------------------------------
  /** Get a typesafe config accessor */
  protected def withConfig[A](f: (Config) => A)(implicit ctx: Context) = f(ctx.config)

  /** Get a optional value by specified key. */
  final protected def getOptionalValue[A](dsn: DataSourceName)
    (f: (Config) => Option[A])(implicit ctx: Context): Option[A] = withConfig { cfg =>
    val root     = cfg.getConfig(dsn.path)
    val section  = root.getConfig(dsn.database)
    getOptionalHostSpecValue(dsn)(f) orElse
    f(section) orElse
    f(root)
  }

  /** Get a optional value by specified key. */
  final protected def getOptionalHostSpecValue[A](dsn: DataSourceName)
    (f: (Config) => Option[A])(implicit ctx: Context): Option[A] = withConfig { cfg =>
    val root     = cfg.getConfig(dsn.path)
    val section  = root.getConfig(dsn.database)
    val hostspec = CF_SECTION_HOSTSPEC.format(dsn.hostspec)
    f(section.getConfig(hostspec)) orElse
    f(root.getConfig(hostspec))
  }
}
