/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import java.time.LocalDateTime

/**
 * The definition for projecting domain model of DDD
 */
trait EntityModel[K <: @@[_, _]] extends Serializable {

  /** The type of entity id */
  type Id         = K
  type WithNoId   = Entity.WithNoId  [Id, this.type]
  type EmbeddedId = Entity.EmbeddedId[Id, this.type]

  /** The entity's identity. */
  val id: Option[Id]

  /** The date and time when this entity was last updated. */
  val updatedAt: LocalDateTime

  /** The date and time when this entity was added to the system. */
  val createdAt: LocalDateTime

  /** convet to a Entiry object. */
  def toWithNoId:   WithNoId   = Entity.WithNoId(this)
  def toEmbeddedId: EmbeddedId = Entity.EmbeddedId(this)
}
