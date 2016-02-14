/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import core.port.adapter.persistence.model.DataSourceName

/** Provides actions */
trait ShadeAction { self: ShadeProfile[_, _] =>

  /** The action request. */
  protected case class ShadeActionRequest(
    val backend: Backend,
    val dsn:     DataSourceName
  ) extends ActionRequest[Backend]


  /** Run the supplied function with a database object. */
  object DBAction extends Action[ShadeActionRequest, Database] {

    /** Invoke the block. */
    def invokeBlock[A](request: ShadeActionRequest, block: Database => Future[A]): Future[A] =
      (for {
        db <- request.backend.getDatabase(request.dsn)
        v  <- block(db)
      } yield v) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=" + request.dsn.toString, ex)
      }

    /** Returns a future of a result */
    def apply[A](dsn: DataSourceName)(block: Database => Future[A])(implicit backend: Backend): Future[A] =
      invokeBlock(ShadeActionRequest(backend, dsn), block)
  }
}
