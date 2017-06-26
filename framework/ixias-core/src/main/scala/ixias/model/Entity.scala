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
final class Entity[M <: EntityModel[_], S <: IdStatus](val data: M)
{
  /** The entity data model */
  type Model     = M

  /** The status of entity's identity */
  type IdStatus  = S

  /** check whether exists entity id value */
  def id(implicit ev: S =:= IdStatus.Exists) = data.id.get

  /** check whether exists entity id value */
  def hasId(implicit ev: TypeTag[IdStatus]): Boolean =
    ev.tpe =:= typeOf[IdStatus.Exists]
}

// Companion object
//~~~~~~~~~~~~~~~~~~~
object Entity {
  /** Create a entity object with embedded id. */
  def apply[M <: EntityModel[_]](data: M): Entity[M, IdStatus.Exists] =
    data.id match {
      case Some(_) => new Entity(data)
      case None    => throw new IllegalArgumentException("Coud not found id on entity's data.")
    }

  /** Create a entity object with no id. */
  def prepare[M <: EntityModel[_]](data: M): Entity[M, IdStatus.Empty] =
    data.id match {
      case None    => new Entity(data)
      case Some(_) => throw new IllegalArgumentException("The entity's id is already setted.")
    }
}
