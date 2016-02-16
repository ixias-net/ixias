/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.action

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import core.util.Logger
import core.port.adapter.persistence.model.DataSourceName
import core.port.adapter.persistence.backend.ShadeBackend

/** Run the supplied function with a database object. */
sealed class ShadeDBAction
    extends Action[DataSourceName, ShadeBackend#Database] {

  /** The logger for profile */
  protected lazy val logger  = Logger.apply

  /** The back-end implementation for this profile */
  protected lazy val backend = ShadeBackend.apply

  /** Invoke the block. */
  def invokeBlock[A](dsn: DataSourceName, block: ShadeBackend#Database => Future[A]): Future[A] =
    (for {
      db <- backend.getDatabase(dsn)
      v  <- block(db)
    } yield v) andThen {
      case Failure(ex) => logger.error("The database action failed. dsn=" + dsn.toString, ex)
    }
}

trait ShadeDBActionProvider {
  object ShadeDBAction {

    /** The back-end type required by this profile */
    type Backend  = ShadeBackend
    /** The type of database objects. */
    type Database = Backend#Database

    /** Returns a future of a result */
    def apply[A](dsn: DataSourceName)(block: Database => Future[A]): Future[A] =
      (new ShadeDBAction).invokeBlock(dsn, block)
  }
}
