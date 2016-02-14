/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import core.port.adapter.persistence.model.{ DataSourceName, Converter }
import core.port.adapter.persistence.backend.BasicBackend
import core.port.adapter.persistence.io.{ IOActionContext, EntityIOActionContext }

/** A builder for generic Actions that generalizes over the type of requests. */
trait ActionFunction[T <: BasicBackend, +P] {

  /** The type of backend object. */
  type Backend = T

  /** Invoke the block.
    * This is the main method that an ActionBuilder has to implement,
    * any other actions, modify the request object or
    * potentially use a different class to represent the request. */
  def invokeBlock[A](backend: Backend, dsn: DataSourceName, block: P => Future[A]): Future[A]

  /** Get the action context to run the request in. */
  protected implicit val IOActionContext = EntityIOActionContext.Implicits.global
}

trait Action[T <: BasicBackend, +P] extends ActionFunction[T, P] {

  /** Constructs an `Action` that returns a future of a result */
  def apply[A](dsn: DataSourceName)
    (block: P => Future[A])(implicit backend: Backend): Future[A] =
    invokeBlock(backend, dsn, block)
}
