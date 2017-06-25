/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import ixias.model._
import scala.language.implicitConversions

trait TaggedOps {
  implicit def taggedToUnwrapValue1[A, _](t: A @@ _): A = Tag.unwrap(t)
  implicit def taggedToUnwrapValue2[A, _](t: Option[A @@ _]): Option[A] = t.map(Tag.unwrap(_))
}
