/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import scala.util.{ Try, Success }
import core.domain.model.{ Identity, Entity }

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K <: Identity[_], E <: Entity[K]] extends IOAction {

  // --[ Read ]-----------------------------------------------------------------
  /** Optionally returns the value associated with a identity. */
  def get(id: K)(implicit ctx: Context): Try[Option[E]]

  /** Returns the value associated with a identity, or
    * a default value if the identity is not contained in the repository. */
  def getOrElse[E1 >: E](id: K, default: K => E1)(implicit ctx: Context): Try[E1] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => default(id)
    })

  /** Retrieves the value which is associated with the given identity.
    * This method invokes the `default` method of the map if there is no mapping
    * from the given identity to a value. */
  def apply(id: K)(implicit ctx: Context): Try[E] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => default(id)
    })

  /** Tests whether this repository contains a binding for a identity. */
  def contains(id: K)(implicit ctx: Context): Try[Boolean] =
    get(id).map(_.isDefined)

  /** Defines the default value computation for the map,
    * returned when a identity is not found The method implemented here throws an exception,
    * but it might be overridden in subclasses. */
  def default(id: K): E =
    throw new NoSuchElementException("identity not found: " + id)

  // --[ Write ]----------------------------------------------------------------
  /** Adds a new identity/entity-value pair to this repository. */
  def add(entity: E)(implicit ctx: Context): Try[K]

  /** If the map already contains a mapping for the identity,
    * it will be overridden by the new value. */
  def update(entity: E)(implicit ctx: Context): Try[Unit]

  /** Removes a identity from this map,
    * returning the value associated previously with that identity as an option. */
  def remove(id: K)(implicit ctx: Context): Try[Option[E]]

  /** If the map already contains a mapping for the identity,
    * it will be overridden by the new value, and returns previously bound value. */
  def put(entity: E)(implicit ctx: Context): Try[Option[E]] = {
    val old = entity.id match {
      case Some(id) => get(id)
      case None     => success(None)
    }
    update(entity)
    old
  }

  /** Adds a new identity/entity-value pair to this repository.
    * If the map already contains a mapping for the identity,
    * it will be overridden by the new value. */
  def addOrUpdate(entity: E)(implicit ctx: Context): Try[E] = {
    entity.id match {
      case None     => add(entity).flatMap(apply)
      case Some(id) => for {
        before <- get(id)
        after  <- before match {
          case None    => add(entity).flatMap(apply)
          case Some(_) => update(entity).flatMap(_ => apply(id))
        }
      } yield(after)
    }
  }

  /** If given identity is already in this map, returns associated value. */
  def getOrElseUpdate(id: K, op: K => E)(implicit ctx: Context): Try[E] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => val entity = op(id); update(entity); entity
    })
}
