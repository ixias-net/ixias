/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.ast.{ TypedType, Library }
import slick.lifted.{ Rep, Query, FunctionSymbolExtensionMethods, CanBeQueryCondition }
import ixias.persistence.model.Cursor

import scala.language.higherKinds
import scala.language.implicitConversions

final case class SlickQueryTransformer[E, U, C[_]](val self: Query[E, U, C]) extends AnyVal {
  def seek(cursor: Cursor): Query[E, U, C] =
    cursor.limit match {
      case None        => if (0 < cursor.offset) self.drop(cursor.offset) else self
      case Some(limit) => self.drop(cursor.offset).take(limit)
    }

  //-- [ Slick3.3 features ] ---------------------------------------------------
  /**
   * Applies the given filter, if the Option value is defined.
   * If the value is None, the filter will not be part of the query.
   */
  def filterOpt[V, T : CanBeQueryCondition](optValue: Option[V])(f: (E, V) => T): Query[E, U, C] =
    optValue.map(v => self.withFilter(a => f(a, v))).getOrElse(self)

  /**
   * Applies the given filter function, if the boolean parameter `p` evaluates to true.
   * If not, the filter will not be part of the query.
   */
  def filterIf[T : CanBeQueryCondition](p: Boolean)(f: E => T): Query[E, U, C] =
    if (p) self.withFilter(f) else self
}

final case class SlickQueryTransformerId[T <: ixias.model.@@[_, _], U, C[_]](
  val self: Query[Rep[T], U, C]
) extends AnyVal {
  def distinctLength(implicit tm: TypedType[Int]): Rep[Int] =
    FunctionSymbolExtensionMethods
      .functionSymbolExtensionMethods(Library.CountDistinct)
      .column(self.toNode)
}

trait SlickQueryOps {
  implicit def toQueryTransformer[E, U, C[_]](a: Query[E, U, C])                                  = SlickQueryTransformer(a)
  implicit def toQueryTransformerId[T <: ixias.model.@@[_, _], U, C[_]](a: Query[Rep[T], U, Seq]) = SlickQueryTransformerId(a)
}
