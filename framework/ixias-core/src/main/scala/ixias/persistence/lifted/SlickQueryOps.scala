/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.lifted.Query
import ixias.persistence.model.Cursor
import scala.language.implicitConversions

final case class SlickQueryTransformer[R, U](val self: Query[R, U, Seq]) extends AnyVal {
  def seek(cursor: Cursor): Query[R, U, Seq] =
    cursor.limit match {
      case None        => if (0 < cursor.offset) self.drop(cursor.offset) else self
      case Some(limit) => self.drop(cursor.offset).take(limit)
    }
}

trait SlickQueryOps {
  implicit def toQueryTransformer[R, U](a: Query[R, U, Seq]) = SlickQueryTransformer(a)
}
