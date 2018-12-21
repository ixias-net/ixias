/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

import scala.concurrent.Future
import ixias.model.{ @@, Entity, EntityModel, IdStatus }
import ixias.persistence.Repository

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K <: @@[_, _], M <: EntityModel[K]]
    extends IOAction { self: Repository[K, M] =>

  /** The type of entity id */
  type Id = K

  /** The type of entity when it has not id. */
  type EntityWithNoId   = Entity[K, M, IdStatus.Empty]

  /** The type of entity when it has embedded id */
  type EntityEmbeddedId = Entity[K, M, IdStatus.Exists]

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Defines the default value computation for the map,
   * returned when a identity is not found The method implemented here throws an exception,
   * but it might be overridden in subclasses.
   */
  def default(id: Id): Future[EntityEmbeddedId] =
    Future.failed(new NoSuchElementException("identity not found: " + id))

  /**
   * Retrieves the value which is associated with the given identity.
   * This method invokes the `default` method of the map if there is no mapping
   * from the given identity to a value.
   */
  def apply(id: Id): Future[EntityEmbeddedId] =
    get(id).flatMap(_ match {
      case Some(v) => Future.successful(v)
      case None    => default(id)
    })

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Optionally returns the value associated with a identity.
   */
  def get(id: Id): Future[Option[EntityEmbeddedId]]

  /**
   * Returns the value associated with a identity, or
   * a default value if the identity is not contained in the repository.
   */
  def getOrElse[E2 >: EntityEmbeddedId](id: Id, f: Id => E2): Future[E2] =
    get(id).map(_.getOrElse(f(id)))

  /**
   * Tests whether this repository contains a binding for a identity.
   */
  def contains(id: Id): Future[Boolean] =
    get(id).map(_.isDefined)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Adds a new identity/entity-value pair to this repository.
   */
  def add(entity: EntityWithNoId): Future[Id]

  /**
   * If the dataset already contains a mapping for the identity,
   * it will be overridden by the new value.
   */
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]]

  /**
   * Removes a identity from this map,
   * returning the value associated previously with that identity as an option.
   */
  def remove(id: Id): Future[Option[EntityEmbeddedId]]
}
