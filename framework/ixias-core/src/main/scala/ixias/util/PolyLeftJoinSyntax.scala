/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import shapeless.{ HList, Poly2 }
import shapeless.ops.hlist.LeftFolder

/**
 * Polymorphic left join operation
 */
case class PolyLeftJoinSyntax[R, P <: Poly2](underlying: R, poly2: P) {

  /**
   * Join operation
   */
  def apply[L <: HList](haystack: L)
    (implicit folder: LeftFolder[L, R, poly2.type]): folder.Out =
    haystack.foldLeft(underlying)(poly2)
}

/**
 * Companion object
 */
object PolyLeftJoinSyntax {

  trait Rule[R] extends Poly2 {

    /**
     * Implicit convert: T => Seq[T]
     */
    implicit def caseAnyRef[T](implicit st: Case[R, Seq[T]]) =
      at[R, T]((root, haystack) => this(root, Seq(haystack)))

    /**
     * Implicit convert: Option[T] => Seq[T]
     */
    implicit def caseOpt[T](implicit st: Case[R, Seq[T]]) =
      at[R, Option[T]]((root, haystack) => this(root, haystack.toSeq))
  }
}
