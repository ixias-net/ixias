/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import shapeless.Unwrapped

/** The Identity of Entity */
trait Identity[T] {
  type U
  def  apply(u: U): T
  def unwrap(t: T): U
}

object Identity {
  type Aux[T, U0] = Identity[T] { type U = U0 }
  implicit def unwrappedIdentity[W, U0](implicit uw: Unwrapped.Aux[W, U0]): Identity.Aux[W, U0] =
    new Identity[W] {
      type U = U0
      def  apply(u: U): W = uw.wrap(u)
      def unwrap(w: W): U = uw.unwrap(w)
    }
}
