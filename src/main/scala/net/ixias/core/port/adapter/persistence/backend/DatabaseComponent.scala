/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import core.util.EnumOf
import core.port.adapter.persistence.io.IOActionContext

trait DatabaseComponent { self =>

  // --[ TypeDefs ]-------------------------------------------------------------
  type This >: this.type <: DatabaseComponent

  /** The type of database source config used by this backend. */
  type DatabaseSouceConfig <: DatabaseSouceConfigDef
  /** The type of the database souce config factory used by this backend. */
  type DatabaseSouceConfigFactory <: DatabaseSouceConfigFactoryDef
  /** The type of the context used for running Database Actions */
  type Context >: Null <: IOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DatabaseSouceConfig: DatabaseSouceConfigFactory

  /** The protocol types used for running IOAction. */
  sealed abstract class Protocol
  object Protocol extends EnumOf[Protocol] {
    case object TCP     extends Protocol
    case object UDP     extends Protocol
    case object Unix    extends Protocol
    case object Unknown extends Protocol
  }

  /** A database souce config instance to which connections can be created. */
  trait DatabaseSouceConfigDef extends Serializable { this: DatabaseSouceConfig =>
    val path:     String
    val protocol: Protocol
    override def toString =
      s"""|${this.getClass.getSimpleName}: {
          |  path:     $path,
          |  protocol: $protocol
          |}""".stripMargin
  }

  /** The factory to create a database source config. */
  trait DatabaseSouceConfigFactoryDef { this: DatabaseSouceConfigFactory =>
    /** Load a configuration for persistent database. */
    def forDSN(name: String): DatabaseSouceConfig
  }
}
