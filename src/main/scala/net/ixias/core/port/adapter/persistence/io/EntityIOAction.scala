/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import core.domain.model.{ Identity, Entity }

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K, E <: Entity[K]] extends IOAction {

  /** The type of entity id */
  type Id = Identity[K]

  /** The type of entity */
  type Entity  = E

  // --[ Methods ]--------------------------------------------------------------
  /** Defines the default value computation for the map,
    * returned when a identity is not found The method implemented here throws an exception,
    * but it might be overridden in subclasses. */
  def default(id: Id): Future[E] =
    Future.failed(new NoSuchElementException("identity not found: " + id))

  /** Retrieves the value which is associated with the given identity.
    * This method invokes the `default` method of the map if there is no mapping
    * from the given identity to a value. */
  def apply(id: Id): Future[E] =
    get(id).flatMap(_ match {
      case Some(v) => Future.successful(v)
      case None    => default(id)
    })

  // --[ Methods ]--------------------------------------------------------------
  /** Optionally returns the value associated with a identity. */
  def get(id: Id): Future[Option[E]]

  /** Returns the value associated with a identity, or
    * a default value if the identity is not contained in the repository. */
  def getOrElse[E2 >: E](id: Id, f: Id => E2): Future[E2] =
    get(id).map(_.getOrElse(f(id)))

  /** Tests whether this repository contains a binding for a identity. */
  def contains(id: Id): Future[Boolean] =
    get(id).map(_.isDefined)

  // --[ Methods ]--------------------------------------------------------------
  /** Adds a new identity/entity-value pair to this repository.
    * If the map already contains a mapping for the identity,
    * it will be overridden by the new value */
  def store(entity: E): Future[Unit]

  /** Removes a identity from this map,
    * returning the value associated previously with that identity as an option. */
  def remove(id: Id): Future[Option[E]]
}
