/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.lifted

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

final case class FutureTransformer[A](val self: Future[A]) extends AnyVal {
  def await(): Unit = await(_ => Unit)
  def await[A1](implicit convert: A => A1): A1 = {
    convert(Await.result(self, Duration.Inf))
  }
}

trait FutureOps {
  implicit def toUnit(a: Future[_]) = Unit
  implicit def toFutureTransformer[A](a: Future[A]) = FutureTransformer(a)
}
