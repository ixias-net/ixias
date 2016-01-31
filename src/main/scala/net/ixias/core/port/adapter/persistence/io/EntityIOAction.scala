/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import scala.util.{ Try, Success }
import core.domain.model._

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K, E <: Entity[K]] extends IOAction {

  /** The type of entity id */
  type Id = Identity[K]

  /** The type of entity */
  type Entity  = E

  // --[ Methods ]--------------------------------------------------------------
  /** Optionally returns the value associated with a identity. */
  def get(id: Id)(implicit ctx: Context): Try[Option[E]]

  /** Returns the value associated with a identity, or
    * a default value if the identity is not contained in the repository. */
  def getOrElse[E2 >: E](id: Id, default: Id => E2)(implicit ctx: Context): Try[E2] =
    get(id).map{ _.getOrElse(default(id)) }

  /** Defines the default value computation for the map,
    * returned when a identity is not found The method implemented here throws an exception,
    * but it might be overridden in subclasses. */
  def default(id: Id): E = throw new NoSuchElementException("identity not found: " + id)

  /** Retrieves the value which is associated with the given identity.
    * This method invokes the `default` method of the map if there is no mapping
    * from the given identity to a value. */
  def apply(id: Id)(implicit ctx: Context): Try[E] =
    get(id).map{ _.getOrElse(default(id)) }

  /** Tests whether this repository contains a binding for a identity. */
  def contains(id: Id)(implicit ctx: Context): Try[Boolean] =
    get(id).map{ _.isDefined }

  // --[ Methods ]--------------------------------------------------------------
  /** Adds a new identity/entity-value pair to this repository. */
  def add(entity: E)(implicit ctx: Context): Try[Id]

  /** If the map already contains a mapping for the identity,
    * it will be overridden by the new value. */
  def update(entity: E)(implicit ctx: Context): Try[Unit]

  /** Removes a identity from this map,
    * returning the value associated previously with that identity as an option. */
  def remove(id: Id)(implicit ctx: Context): Try[Option[E]]

  /** If the map already contains a mapping for the identity,
    * it will be overridden by the new value, and returns previously bound value. */
  def put(entity: E)(implicit ctx: Context): Try[Option[E]] = {
    val old = entity.id match {
      case SomeId(_) => get(entity.id)
      case NoneId    => Success(None)
    }
    update(entity).flatMap(_ => old)
  }

  /** Adds a new identity/entity-value pair to this repository.
    * If the map already contains a mapping for the identity,
    * it will be overridden by the new value. */
  def addOrUpdate(entity: E)(implicit ctx: Context): Try[E] = {
    entity.id match {
      case NoneId    => add(entity).flatMap(apply)
      case SomeId(_) => for {
        before <- get(entity.id)
        after  <- before match {
          case None    => add(entity).flatMap(apply)
          case Some(_) => update(entity).flatMap(_ => apply(entity.id))
        }
      } yield(after)
    }
  }

  /** If given identity is already in this map, returns associated value. */
  def getOrElseUpdate(id: Id, op: Id => E)(implicit ctx: Context): Try[E] =
    get(id).map(_ match {
      case Some(e) => e
      case None    => {
        val entity = op(id)
        add(entity).map(_ => entity).get
      }
    })
}
