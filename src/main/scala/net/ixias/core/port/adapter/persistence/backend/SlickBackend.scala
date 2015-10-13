/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import java.sql.Connection
import slick.jdbc.JdbcDataSource
import slick.driver.JdbcProfile
import core.util.ConfigExt._

trait SlickBackend[P <: JdbcProfile] extends BasicBackend {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of Slick Jdbc Driver. */
  type Driver = P
  /** The type of database objects used by this backend. */
  type Database = P#Backend#Database
  /** The type of the database factory used by this backend. */
  type DatabaseFactory = DatabaseFactoryDef
  /** The type of JDBC data-souce objects. */
  type DataSource = HikariCPDataSourceDef
  /** The type of the JDBC data-source factory. */
  type DataSourceFactory = HikariCPDatabaseSouceFactoryDef

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val Database = new DatabaseFactoryDef{}
  /** The data-souce factory */
  val DataSource = new HikariCPDatabaseSouceFactoryDef{}

  // --[ Methods ]--------------------------------------------------------------
  /** Get a Database instance from connection pool. */
  def getDatabase(driver: Driver, dsn: String)(implicit ctx: Context): Database =
    Database.getDatabase(driver, dsn)

  // --[ Database ]-------------------------------------------------------------
  trait DatabaseFactoryDef {
    import scala.collection.mutable.Map
    protected var stock: Map[String, Database] = Map.empty
    def getDatabase(driver: Driver, dsn: String)(implicit ctx: Context): Database = {
      val insensitive = dsn.toLowerCase
      stock.getOrElseUpdate(insensitive, {
        val ds = DataSource.forDSN(insensitive)
        driver.backend.Database.forSource(ds)
      })
    }
  }

  // --[ DataSource ]-----------------------------------------------------------
  /** A database souce instance to which connections can be created. */
  case class HikariCPDataSourceDef (
    val ds:    com.zaxxer.hikari.HikariDataSource,
    val hconf: com.zaxxer.hikari.HikariConfig
  ) extends JdbcDataSource {

    /** Create a new Connection or get one from the pool */
    def createConnection(): Connection = ds.getConnection()

    /** If this object represents a connection pool managed directly by Slick, close it.
      * Otherwise no action is taken. */
    def close(): Unit = ds.close()
  }

  /** Factory methods for creating `DatabSouce` instances. */
  trait DatabaseSouceFactoryDef {
    def forDSN(dsn: String)(implicit ctx: Context): JdbcDataSource
  }

  /** Factory methods for creating `DatabSouce` instances with using HikariCP. */
  trait HikariCPDatabaseSouceFactoryDef extends DatabaseSouceFactoryDef {
    import com.zaxxer.hikari._

    /** Create a JdbcDataSource from DSN (Database Souce Name) */
    def forDSN(dsn: String)(implicit ctx: Context): DataSource = {
      // Parse Data-Source-Name.
      val dsconf = DatabaseSouceConfig.forDSN(dsn)
      if (dsconf.database.isEmpty || dsconf.hostspec.isEmpty) {
        throw new Exception(s"""Not found database or hostspec on DSN. { dsn: $dsn }""")
      }

      // Build config for HikariCP.
      val hconf = new HikariConfig()
      val conf  = ctx.conf.getConfig(dsconf.path)
      val prefixes = Seq( s"",
        s"""hostspec.${dsconf.hostspec.get}.""",
        s"""${dsconf.database.get}.hostspec.${dsconf.hostspec.get}.""")
      if ( !conf.hasPath(dsconf.database.get)
        || !conf.hasPath(dsconf.database.get + ".hostspec." + dsconf.hostspec.get)) {
        throw new Exception(s"""Not found specified hostspec's configuratin values. { dsn: $dsn }""")
      }

      // Connection settings
      prefixes.foreach { prefix =>
        conf.getStringOpt(prefix + "user").foreach(hconf.setUsername)
        conf.getStringOpt(prefix + "password").foreach(hconf.setPassword)
        conf.getStringOpt(prefix + "driverClassName").foreach(hconf.setDriverClassName)
        conf.getStringOpt(prefix + "connectionInitSql").foreach(hconf.setConnectionInitSql)
        conf.getBooleanOpt(prefix + "readonly").foreach(hconf.setReadOnly)
        conf.getPropertiesOpt(prefix + "properties").foreach(hconf.setDataSourceProperties)
      }
      val path = s"""${dsconf.database.get}.hostspec.${dsconf.hostspec.get}.hosts"""
      val database = conf.getStringOr(s"""${dsconf.database.get}.database""", "")
      val url = conf.getAnyRef(path) match {
        case v:  String                       => s"""jdbc:mysql://%s/%s""".format(v, database)
        case v: List[_] if hconf.isReadOnly() => s"""jdbc:mysql:loadbalance://%s/%s""".format(v.mkString(","), database)
        case v => throw new Exception(s"""Illegal value type of host setting. { $path: $v }""")
      }
      hconf.setJdbcUrl(url)

      // Pool configuration
      hconf.setPoolName(dsn)
      hconf.setMinimumIdle(5)
      hconf.setMaximumPoolSize(10)
      hconf.setConnectionTestQuery("SELECT 1")
      hconf.setConnectionTimeout(conf.getMillisecondsOr("connectionTimeout", 1000))
      hconf.setValidationTimeout(conf.getMillisecondsOr("validationTimeout", 1000))
      hconf.setIdleTimeout(conf.getMillisecondsOr("idleTimeout",  600000))
      hconf.setMaxLifetime(conf.getMillisecondsOr("maxLifetime", 1800000))
      hconf.setLeakDetectionThreshold(conf.getMillisecondsOr("leakDetectionThreshold", 0))
      hconf.setInitializationFailFast(conf.getBooleanOr("initializationFailFast", false))
      hconf.setRegisterMbeans(conf.getBooleanOr("registerMbeans", false))
      prefixes.foreach { prefix =>
        conf.getIntOpt(prefix + "minConnections").foreach(hconf.setMaximumPoolSize)
        conf.getIntOpt(prefix + "maxConnections").foreach(hconf.setMinimumIdle)
      }
      HikariCPDataSourceDef(new HikariDataSource(hconf), hconf)
    }
  }
}

