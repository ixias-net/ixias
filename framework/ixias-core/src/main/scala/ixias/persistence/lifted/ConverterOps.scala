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
  implicit def toModelToEntity[K <: @@[_, _], M <: EntityModel[K]]
    (m: M): Entity.EmbeddedId[K, M] = Entity.EmbeddedId[K, M](m)

  // for Seq[EntityModel]
  implicit def toModelToEntitySeq[K <: @@[_, _], M <: EntityModel[K]]
    (m: Seq[M]): Seq[Entity.EmbeddedId[K, M]] = m.map(Entity.EmbeddedId[K, M](_))

  // for Option[EntityModel]
  implicit def toModelToEntityOpt[K <: @@[_, _], M <: EntityModel[K]]
    (m: Option[M]): Option[Entity.EmbeddedId[K, M]] = m.map(Entity.EmbeddedId[K, M](_))

  implicit def convert[A, B](o: A)(implicit conv: Converter[A, B]): B = conv.convert(o)
}
