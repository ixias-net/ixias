/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import scala.language.higherKinds

object Tag {
  type Id[X] = X

  /** `subst` specialized to `Id`. */
  @inline def apply[@specialized A, T](a: A): A @@ T = a.asInstanceOf[A @@ T]

  /** `unsubst` specialized to `Id`. */
  @inline def unwrap[@specialized A, T](a: A @@ T): A = unsubst[A, Id, T](a)

  /** Remove the tag `T`, leaving `A`. */
  def unsubst[A, F[_], T](fa: F[A @@ T]): F[A] = fa.asInstanceOf[F[A]]

  /** @see `Tag.of` */
  final class TagOf[T] private[Tag]() {
    /** Like `Tag.apply`, but specify only the `T`. */
    def apply[A](a: A): A @@ T = Tag.apply(a)
    /** Like `Tag.unwrap`, but specify only the `T`. */
    def unwrap[A](a: A @@ T): A = Tag.unwrap(a)
    /** Like `Tag.unsubst`, but specify only the `T`. */
    def unsubst[F[_], A](fa: F[A @@ T]): F[A] = Tag.unsubst(fa)
  }

  /**
   * Variants of `apply`, `subst`, and `unsubst` that require
   * specifying the tag type but are more likely to infer the other type parameters.
   */
  def of[T]: TagOf[T] = new TagOf[T]
}
