/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import scala.reflect.runtime.universe._

/** Entity's id Status */
trait  IdStatus
object IdStatus {
  trait Empty  extends IdStatus
  trait Exists extends IdStatus
}

/** The Entity */
trait Entity[K <: @@[_, _], M <: EntityModel[K], S <: IdStatus] {

  /** The entity data model */
  type Model    = M

  /** The status of entity's identity */
  type IdStatus = S

  /** The entity's values */
  val v: M

  /** get id value whene id is exists */
  def id(implicit ev: S =:= IdStatus.Exists): K = v.id.get

  /** check whether exists entity id value */
  def hasId(implicit ev: TypeTag[IdStatus]): Boolean =
    ev.tpe =:= typeOf[IdStatus.Exists]
}
final class   EntityWithNoId[K <: @@[_, _], M <: EntityModel[K]](val v: M) extends Entity[K, M, IdStatus.Empty]
final class EntityEmbeddedId[K <: @@[_, _], M <: EntityModel[K]](val v: M) extends Entity[K, M, IdStatus.Exists]

/**
 * Create a entity object with no id.
 */
object EntityWithNoId {
  def apply[K <: @@[_, _], M <: EntityModel[K]](data: M): EntityWithNoId[K, M] =
    data.id match {
      case None    =>       new EntityWithNoId(data)
      case Some(_) => throw new IllegalArgumentException("The entity's id is already setted.")
    }
}

/**
 * Create a entity object with embedded id.
 */
object EntityEmbeddedId {
  def apply[K <: @@[_, _], M <: EntityModel[K]](data: M): EntityEmbeddedId[K, M] =
    data.id match {
      case Some(_) =>       new EntityEmbeddedId(data)
      case None    => throw new IllegalArgumentException("Coud not found id on entity's data.")
    }
}
