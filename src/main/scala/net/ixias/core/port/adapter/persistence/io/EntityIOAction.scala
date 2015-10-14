/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import scalaz._
import scalaz.Scalaz._
import core.domain.model.{ Identity, Entity }

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K <: Identity[_], V <: Entity[K]] extends IOAction {

  /** Optionally returns the value associated with a identity. */
  def get(id: K)(implicit ctx: Context): ValidationNel[Option[V]]

  /** Adds a new identity/entity-value pair to this repository.
    * If the map already contains a mapping for the identity,
    * it will be overridden by the new value. */
  def update(entity: V)(implicit ctx: Context): Unit

  /** Removes a identity from this map,
    * returning the value associated previously with that identity as an option. */
  def remove(id: K)(implicit ctx: Context): ValidationNel[Option[V]]

  /** Defines the default value computation for the map,
    * returned when a identity is not found The method implemented here throws an exception,
    * but it might be overridden in subclasses. */
  def default(id: K): V =
    throw new NoSuchElementException("identity not found: " + id)

  /** Returns the value associated with a identity, or
    * a default value if the identity is not contained in the repository. */
  def getOrElse[V1 >: V](id: K, default: K => V1)(implicit ctx: Context): ValidationNel[V1] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => default(id)
    })

  /** Retrieves the value which is associated with the given identity.
    * This method invokes the `default` method of the map if there is no mapping
    * from the given identity to a value. */
  def apply(id: K)(implicit ctx: Context): ValidationNel[V] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => default(id)
    })

  /** Tests whether this repository contains a binding for a identity. */
  def contains(id: K)(implicit ctx: Context): ValidationNel[Boolean] =
    get(id).map(_.isDefined)

  /** Adds a new identity/entity-value pair to this repository and
    * optionally returns previously bound value.
    * If the map already contains a mapping for the identity, it will be overridden by the new value. */
  def put(entity: V)(implicit ctx: Context): ValidationNel[Option[V]] = {
    val old = entity.id match {
      case Some(id) => get(id)
      case None     => None.success
    }
    update(entity)
    old
  }

  /** If given identity is already in this map, returns associated value. */
  def getOrElseUpdate(id: K, op: K => V)(implicit ctx: Context): ValidationNel[V] =
    get(id).map(_ match {
      case Some(v) => v
      case None    => val entity = op(id); update(entity); entity
    })
}
