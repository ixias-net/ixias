/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import slick.dbio.{ DBIOAction, NoStream, Effect }
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

final case class SlickDBIOActionTransformer[K <: @@[_, _], M <: EntityModel[K], E <: Effect]
  (val self: DBIOAction[Seq[M], NoStream, E]) extends AnyVal
{
  def toEntitySeq(implicit ex: ExecutionContext): DBIOAction[Seq[Entity.EmbeddedId[K, M]], NoStream, E] =
    self.map(_.map(Entity.EmbeddedId[K, M](_)))
}

trait SlickDBIOActionOps[K <: @@[_, _], M <: EntityModel[K]] {
  implicit def toDBIOActionTransformer[E <: Effect](a: DBIOAction[Seq[M], NoStream, E]) =
    SlickDBIOActionTransformer[K, M, E](a)
}
