/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import scala.collection.JavaConverters._
import ixias.util.Configuration
import ixias.persistence.model.DataSourceName

trait BasicDatabaseConfig {

  /** The section format */
  protected val CF_SECTION_HOSTSPEC  = """hostspec.%s"""

  /** The keys of configuration */
  protected val CF_USERNAME          = "username"
  protected val CF_PASSWORD          = "password"
  protected val CF_DRIVER_CLASS_NAME = "driver_class_name"
  protected val CF_HOSTSPEC_HOSTS    = "hosts"
  protected val CF_HOSTSPEC_DATABASE = "database"
  protected val CF_HOSTSPEC_SCHEMA   = "schema"
  protected val CF_HOSTSPEC_READONLY = "readonly"

  /** The configuration */
  protected val config = Configuration()

  // --[ Configuration ]--------------------------------------------------------
  /**
   * Get a value by specified key.
   */
  final protected def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    Seq(
      dsn.path + "." + dsn.database + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path + "." + dsn.database,
      dsn.path + "." + CF_SECTION_HOSTSPEC.format(dsn.hostspec),
      dsn.path
    ).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse {
        config.get[Option[Configuration]](path).flatMap(f(_))
      }
    }

  // --[ Methods ]--------------------------------------------------------------
  /** Get the username used for DataSource */
  protected def getUserName(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_USERNAME))

  /** Get the password used for DataSource */
  protected def getPassword(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_PASSWORD))

  /** Get the flag for connection in read-only mode. */
  protected def getHostSpecReadOnly(implicit dsn: DataSourceName): Option[Boolean] =
    readValue(_.get[Option[Boolean]](CF_HOSTSPEC_READONLY))

  // --[ Methods ]--------------------------------------------------------------
  /** Get the JDBC driver class name. */
  protected def getDriverClassName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](CF_DRIVER_CLASS_NAME)).get)

  /** Get the database name. */
  protected def getDatabaseName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](CF_HOSTSPEC_DATABASE)).get)

  /** Get the schema name. */
  protected def getSchemaName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](CF_HOSTSPEC_SCHEMA)).get)

  /** Get host list to connect to database. */
  protected def getHosts(implicit dsn: DataSourceName): Try[Seq[String]] = {
    val path    = dsn.path + '.' + dsn.database + '.' + CF_SECTION_HOSTSPEC.format(dsn.hostspec)
    val section = config.get[Configuration](path).underlying
    val opt     = section.getAnyRef(CF_HOSTSPEC_HOSTS) match {
      case v: String            => Seq(v)
      case v: java.util.List[_] => v.asScala.toList.map(_.toString)
      case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
    }
    Try(opt)
  }
}
