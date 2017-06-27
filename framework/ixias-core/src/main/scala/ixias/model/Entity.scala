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
final class Entity[K <: @@[_, _], M <: EntityModel[K], S <: IdStatus](val v: M) {

  /** The entity data model */
  type Model    = M

  /** The status of entity's identity */
  type IdStatus = S

  /** get id value whene id is exists */
  def id(implicit ev: S =:= IdStatus.Exists): K = v.id.get

  /** check whether exists entity id value */
  def hasId(implicit ev: TypeTag[IdStatus]): Boolean =
    ev.tpe =:= typeOf[IdStatus.Exists]
}

// Companion object for Entity
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object Entity {

  // Entity with no identity.
  //~~~~~~~~~~~~~~~~~~~~~~~~~~
  type   WithNoId[K <: @@[_, _], M <: EntityModel[K]] = Entity[K, M, IdStatus.Empty]
  object WithNoId {
    /** Create a entity object with no id. */
    def apply[K <: @@[_, _], M <: EntityModel[K]](data: M): WithNoId[K, M] =
      data.id match {
        case None    =>       new Entity(data)
        case Some(_) => throw new IllegalArgumentException("The entity's id is already setted.")
      }
  }

  // Entity has embedded Id.
  //~~~~~~~~~~~~~~~~~~~~~~~~~~
  type   EmbeddedId[K <: @@[_, _], M <: EntityModel[K]] = Entity[K, M, IdStatus.Exists]
  object EmbeddedId {
    def apply[K <: @@[_, _], M <: EntityModel[K]](data: M): EmbeddedId[K, M] =
      data.id match {
        case Some(_) =>       new Entity(data)
        case None    => throw new IllegalArgumentException("Coud not found id on entity's data.")
      }
  }
}
