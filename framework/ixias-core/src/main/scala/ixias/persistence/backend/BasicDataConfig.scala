/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import scala.collection.JavaConverters._
import ixias.util.Configuration
import ixias.persistence.model.DataSourceName

trait BasicDataConfig {

  /** The section format */
  protected val CF_SECTION_HOSTSPEC  = """hostspec.%s"""

  /** The keys of configuration */
  protected val CF_USERNAME          = "username"
  protected val CF_PASSWORD          = "password"
  protected val CF_DRIVER_CLASS_NAME = "driver_class_name"
  protected val CF_HOSTSPEC_HOSTS    = "hosts"
  protected val CF_HOSTSPEC_DATABASE = "database"
  protected val CF_HOSTSPEC_READONLY = "readonly"

  /** The configuration */
  protected val config = Configuration()

  // --[ Configuration ]--------------------------------------------------------
  /**
   * Get a value by specified key.
   */
  final protected def readValue[A](dsn: DataSourceName)(f: Configuration => Option[A]): Option[A] =
    Seq(
      dsn.path + "." + dsn.database + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path + "." + dsn.database,
      dsn.path + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path
    ).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse(config.getConfig(path).flatMap(f))
    }

  // --[ Methods ]--------------------------------------------------------------
  /** Get the username used for DataSource */
  protected def getUserName(dsn: DataSourceName): Option[String] =
    readValue(dsn)(_.getString(CF_USERNAME))

  /** Get the password used for DataSource */
  protected def getPassword(dsn: DataSourceName): Option[String] =
    readValue(dsn)(_.getString(CF_PASSWORD))

  /** Get the flag for connection in read-only mode. */
  protected def getHostSpecReadOnly(dsn: DataSourceName): Option[Boolean] =
    readValue(dsn)(_.getBoolean(CF_HOSTSPEC_READONLY))

  // --[ Methods ]--------------------------------------------------------------
  /** Get the JDBC driver class name. */
  protected def getDriverClassName(dsn: DataSourceName): Try[String] =
    Try(readValue(dsn)(_.getString(CF_DRIVER_CLASS_NAME)).get)

  /** Get the database name. */
  protected def getDatabaseName(dsn: DataSourceName): Try[String] =
    Try(readValue(dsn)(_.getString(CF_HOSTSPEC_DATABASE)).get)

  /** Get host list to connect to database. */
  protected def getHosts(dsn: DataSourceName): Try[Seq[String]] = {
    val path = dsn.path + '.' + dsn.database + '.' + CF_SECTION_HOSTSPEC.format(dsn.hostspec)
    val opt  = config.getConfig(path).map { section =>
      section.underlying.getAnyRef(CF_HOSTSPEC_HOSTS) match {
        case v: String            => Seq(v)
        case v: java.util.List[_] => asScalaBufferConverter(v).asScala.toList.map(_.toString)
        case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
      }
    }
    Try(opt.get)
  }
}
