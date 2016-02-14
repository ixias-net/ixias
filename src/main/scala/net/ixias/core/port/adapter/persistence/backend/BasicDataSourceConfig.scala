/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.util.Try
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import com.typesafe.config.Config

import core.port.adapter.persistence.model.DataSourceName
import core.port.adapter.persistence.io.EntityIOActionContext

trait BasicDataSourceConfig { self: BasicDataSource =>

  /** The section format */
  protected val CF_SECTION_HOSTSPEC       = """hostspec.%s"""

  /** The keys of configuration */
  protected val CF_USERNAME               = "username"
  protected val CF_PASSWORD               = "password"
  protected val CF_DRIVER_CLASS_NAME      = "driver_class_name"
  protected val CF_HOSTSPEC_HOSTS         = "hosts"
  protected val CF_HOSTSPEC_DATABASE      = "database"
  protected val CF_HOSTSPEC_READONLY      = "readonly"

  // --[ Methods ]--------------------------------------------------------------
  /** Get the username used for DataSource */
  protected def getUserName
    (dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getString(CF_USERNAME))

  /** Get the password used for DataSource */
  protected def getPassword
    (dsn: DataSourceName)(implicit ctx: Context): Option[String] =
    getOptionalValue(dsn)(_.getString(CF_PASSWORD))

  /** Get the flag for connection in read-only mode. */
  protected def getHostSpecReadOnly
    (dsn: DataSourceName)(implicit ctx: Context): Option[Boolean] =
    getOptionalValue(dsn)(_.getBoolean(CF_HOSTSPEC_READONLY))

  // --[ Methods ]--------------------------------------------------------------
  /** Get the JDBC driver class name. */
  protected def getDriverClassName
    (dsn: DataSourceName)(implicit ctx: Context): Try[String] =
    getValue(dsn)(_.getString(CF_DRIVER_CLASS_NAME))

  /** Get the database name. */
  protected def getDatabaseName
    (dsn: DataSourceName)(implicit ctx: Context): Try[String] =
    getValue(dsn)(_.getString(CF_HOSTSPEC_DATABASE))

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
  /** Get a value by specified key. */
  final protected def getValue[A]
    (dsn: DataSourceName)(f: (Config) => A)(implicit ctx: Context): Try[A] =
    withConfig { cfg =>
      val haystack = Seq(
        dsn.path + "." + dsn.database + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
        dsn.path + "." + dsn.database,
        dsn.path + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
        dsn.path
      )
      getValue(cfg, haystack)(f)
    }

  /** Get a optional value by specified key. */
  final protected def getOptionalValue[A]
    (dsn: DataSourceName)(f: (Config) => A)(implicit ctx: Context): Option[A] =
    getValue(dsn)(f).toOption

  /** Get a value by specified key. */
  private def getValue[A]
    (root: Config, haystack: Seq[String])(f: (Config) => A): Try[A] =
    haystack.size match {
      case 1 => Try(f(root.getConfig(haystack.head)))
      case _ => Try(f(root.getConfig(haystack.head))) orElse getValue(root, haystack.tail)(f)
    }

  /** Get a typesafe config accessor */
  protected def withConfig[A](f: (Config) => A)(implicit ctx: Context) = f(ctx.config)
}
