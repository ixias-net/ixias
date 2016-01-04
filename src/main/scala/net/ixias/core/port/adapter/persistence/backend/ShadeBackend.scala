/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import shade.memcached.{ Memcached, Configuration }
import core.util.ConfigExt._

trait ShadeBackend extends BasicBackend {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database objects used by this backend. */
  type Database = Memcached
  /** The type of the database factory used by this backend. */
  type DatabaseFactory = DatabaseFactoryDef
  /** The type of memcached data-souce objects. */
  type DataSource = Configuration
  /** The type of the memcached data-source factory. */
  type DataSourceFactory = MemcachedDatabaseSouceFactoryDef

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  lazy val Database   = new DatabaseFactoryDef{}
  /** The data-souce factory */
  lazy val DataSource = new MemcachedDatabaseSouceFactoryDef{}

  // --[ Methods ]--------------------------------------------------------------
  /** To initialize a Memcached client. */
  def getDatabase(dsn: String)(implicit ctx: Context) = Database.getDatabase(dsn)

  // --[ Database ]-------------------------------------------------------------
  trait DatabaseFactoryDef {
    def getDatabase(dsn: String)(implicit ctx: Context): Database = ???
  }
  trait MemcachedDatabaseSouceFactoryDef {
    /** Create a memcached configuration object from DSN (Database Souce Name) */
    def forDSN(dsn: String)(implicit ctx: Context): DataSource = ???
  }
}
