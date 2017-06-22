/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import java.time.LocalDateTime
import scala.reflect.runtime.universe._

trait  IdStatus
object IdStatus {
  trait Empty  extends IdStatus
  trait Exists extends IdStatus
}

trait Entity[K <: Tagged[_, _], S <: IdStatus] extends Serializable
{
  /** The type of entity id */
  type Id   = K

  /** The status of entity's identity. */
  type IdSt = S

  /** The entity's identity. */
  val _id: Id

  /** The current version of the object. Used for optimistic concurrency versioning. */
  val version: Option[Long] = None

  /** The date and time when this entity was last updated. */
  val updatedAt: LocalDateTime = LocalDateTime.now()

  /** The date and time when this entity was added to the system. */
  val createdAt: LocalDateTime = LocalDateTime.now()

  /** check whether exists entity id value. */
  def id(implicit ev: IdSt =:= IdStatus.Exists): Id = _id

  /** get entity id value as `Option[Id]`. */
  def idOpt(implicit ev: TypeTag[IdSt]): Option[Id] =
    if (hasId) Some(_id) else None

  /** check whether exists entity id value. */
  def hasId(implicit ev: TypeTag[IdSt]): Boolean =
    ev.tpe =:= typeOf[IdStatus.Exists]
}

/** should an entity class always have an ID value. */
trait EntityFixiedId[T <: Tagged[_, _]]
    extends Entity[T, IdStatus.Exists] with Serializable
