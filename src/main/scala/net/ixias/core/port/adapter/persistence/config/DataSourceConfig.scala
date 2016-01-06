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

  /** The section format */
  protected val CF_SECTION_HOSTSPEC       = """hostspec.%s"""

  /** The keys of configuration */
  protected val CF_USER                   = """user"""
  protected val CF_PASSWORD               = """password"""
  protected val CF_DRIVER_CLASS_NAME      = """driver_class_name"""
  protected val CF_HOSTSPEC_HOSTS         = """hosts"""
  protected val CF_HOSTSPEC_READONLY      = """readonly"""

  // --[ Methods ]--------------------------------------------------------------
  /** Get the username used for DataSource */
  protected def getUser
    (dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_USER))

  /** Get the password used for DataSource */
  protected def getPassword
    (dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_PASSWORD))

  /** Get the JDBC driver class name. */
  protected def getDriverClassName
    (dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getStringOpt(CF_DRIVER_CLASS_NAME))

  /** Get the flag for connection in read-only mode. */
  protected def getHostSpecReadOnly
    (dsn: DataSourceName)(implicit ctx: Context): Option[Boolean] =
    getOptionalValue(dsn)(_.getBooleanOpt(CF_HOSTSPEC_READONLY))

  /** Get host list to connect to database. */
  protected def getHosts
    (dsn: DataSourceName)(implicit ctx: Context): Try[List[String]] =
    withConfig { cfg =>
      val path = dsn.path + '.' + dsn.database + '.' + CF_SECTION_HOSTSPEC.format(dsn.hostspec)
      Try(cfg.getConfig(path).getAnyRef(CF_HOSTSPEC_HOSTS)).map(_ match {
        case v: String => List(v)
        case v: java.util.List[_] => v.toList.asInstanceOf[List[String]]
        case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
      })
    }

  // --[ Configuration ]--------------------------------------------------------
  /** Get a typesafe config accessor */
  protected def withConfig[A](f: (Config) => A)(implicit ctx: Context) = f(ctx.config)

  /** Get a optional value by specified key. */
  final protected def getOptionalValue[A]
    (dsn: DataSourceName)(f: (Config) => Option[A])(implicit ctx: Context): Option[A] =
    withConfig { cfg =>
      val paths = Seq(
        dsn.path + '.' + dsn.database + '.' + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
        dsn.path + '.' + dsn.database,
        dsn.path + '.' + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
        dsn.path
      )
      getOptionalValue(cfg, paths)(f)
    }

  /** Get a optional value by specified key. */
  final protected def getOptionalValue[A]
    (root: Config, list: Seq[String])(f: (Config) => Option[A]): Option[A] =
    if (list.isEmpty) None else
      f(root.getConfig(list.head)) orElse getOptionalValue(root, list.tail)(f)
}
