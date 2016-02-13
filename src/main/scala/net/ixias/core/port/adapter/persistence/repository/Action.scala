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

import core.port.adapter.persistence.model.Converter
import core.port.adapter.persistence.backend.BasicBackend
import core.port.adapter.persistence.io.{ IOActionContext, EntityIOActionContext }

trait Action[T <: BasicBackend, P] {

  /** The type of backend object. */
  type Backend = T

  /** Invokes this action. */
  def apply(backend: Backend): Future[P]
}

/** A builder for generic Actions that generalizes over the type of requests. */
trait ActionFunction[T <: BasicBackend, +P] {

  /** The type of backend object. */
  type Backend = T

  /** Invoke the block.
    * This is the main method that an ActionBuilder has to implement,
    * any other actions, modify the request object or
    * potentially use a different class to represent the request. */
  def invokeBlock[A](backend: Backend, block: P => Future[A]): Future[A]

  /** Get the action context to run the request in. */
  protected def IOActionContext: IOActionContext = EntityIOActionContext.Implicits.global
}

trait ActionBuilder[T <: BasicBackend, +P] extends ActionFunction[T, P] {

  /** Constructs an `Action` that returns a future of a result */
  def apply[A, B](block: P => Future[A])
    (implicit backend: Backend, conv: Converter[A, B]): Action[T, B] = new Action[T, B] {
    def apply(backend: Backend): Future[B] = {
      invokeBlock(backend, block).map(conv.convert)
    }
  }
}
