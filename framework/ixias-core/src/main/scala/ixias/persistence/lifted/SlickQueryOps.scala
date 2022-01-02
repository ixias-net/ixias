/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.ast.{ TypedType, Library }
import slick.lifted.{ Rep, Query, FunctionSymbolExtensionMethods }
import ixias.persistence.model.Cursor
import scala.language.implicitConversions

final case class SlickQueryTransformer[R, U](val self: Query[R, U, Seq]) extends AnyVal {
  def seek(cursor: Cursor): Query[R, U, Seq] =
    cursor.limit match {
      case None        => if (0 < cursor.offset) self.drop(cursor.offset) else self
      case Some(limit) => self.drop(cursor.offset).take(limit)
    }
}

final case class SlickQueryTransformerId[T <: ixias.model.@@[_, _], U](
  val self: Query[Rep[T], U, Seq]
) extends AnyVal {
  def distinctLength(implicit tm: TypedType[Int]): Rep[Int] =
    FunctionSymbolExtensionMethods
      .functionSymbolExtensionMethods(Library.CountDistinct)
      .column(self.toNode)
}

trait SlickQueryOps {
  implicit def toQueryTransformer[R, U](a: Query[R, U, Seq]) =
    SlickQueryTransformer(a)
  implicit def toQueryTransformerId[T <: ixias.model.@@[_, _], U](a: Query[Rep[T], U, Seq]) =
    SlickQueryTransformerId(a)
}
