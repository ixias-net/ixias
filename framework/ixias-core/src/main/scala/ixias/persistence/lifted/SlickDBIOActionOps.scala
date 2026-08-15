/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import slick.dbio.{ DBIOAction, NoStream, Effect }
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.reflect.ClassTag

final case class SlickDBIOActionTransformer[K <: @@[_, _], M <: EntityModel[K], E <: Effect]
  (val self: DBIOAction[_, NoStream, E]) extends AnyVal
{
  // Seq[M] => Seq[Entity.EmbeddedId[K, M]]
  def toEntity(implicit ctag: ClassTag[Seq[M]], ex: ExecutionContext):
      DBIOAction[Seq[Entity.EmbeddedId[K, M]], NoStream, E] = self collect {
    case itr if ctag.runtimeClass.isInstance(itr) =>
      itr.asInstanceOf[Seq[M]].map(Entity.EmbeddedId[K, M](_))
  }

  // Seq[M] => Seq[R2]
  def mapEntity[R2](fn: Entity.EmbeddedId[K, M] => R2)(implicit ctag: ClassTag[Seq[M]], ex: ExecutionContext):
      DBIOAction[Seq[R2], NoStream, E] = self collect {
    case itr if ctag.runtimeClass.isInstance(itr) =>
      itr.asInstanceOf[Seq[M]].map(m => fn(Entity.EmbeddedId[K, M](m)))
  }
}

trait SlickDBIOActionOps[K <: @@[_, _], M <: EntityModel[K]] {
  implicit def toDBIOActionTransformer[E <: Effect](a: DBIOAction[Seq[M], NoStream, E]): SlickDBIOActionTransformer[K, M, E] =
    SlickDBIOActionTransformer[K, M, E](a)
}
