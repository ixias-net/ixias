/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import ixias.persistence.model.Converter
import scala.language.implicitConversions

trait ConverterOps
{
  // for EntityModel
  implicit def toModelToEntity[M <: EntityModel]
    (m: M): Entity.EmbeddedId[M] = Entity.EmbeddedId[M](m)

  // for Seq[EntityModel]
  implicit def toModelToEntitySeq[M <: EntityModel]
    (m: Seq[M]): Seq[Entity.EmbeddedId[M]] = m.map(Entity.EmbeddedId[M](_))

  // for Option[EntityModel]
  implicit def toModelToEntityOpt[M <: EntityModel]
    (m: Option[M]): Option[Entity.EmbeddedId[M]] = m.map(Entity.EmbeddedId[M](_))

  implicit def convert[A, B](o: A)(implicit conv: Converter[A, B]): B = conv.convert(o)
}
