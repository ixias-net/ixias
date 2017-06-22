/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import scala.language.implicitConversions

final case class TaggedTransformer[A, T](self: A @@ T) extends AnyVal {
  @inline def unwrap: A = Tag.unwrap(self)
}

trait TaggedOps {
  implicit def toTaggedTransformer[A, T](a: A @@ T) = TaggedTransformer(a)
}
