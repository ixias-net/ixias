/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.model

import scala.reflect.ClassTag
import core.domain.model.Identity

trait TableWrites[-A, B] {

  /** Convert the object into a table record */
  def writes(o: A): B
}

object TableWrites extends TableDefaultWrites {
}


/** Default Serializers. */
trait TableDefaultWrites {
  import scala.language.implicitConversions

  /** Serializer for Seq[T] types. */
  implicit def SeqWrites[A: ClassTag, B: ClassTag](implicit fmt: TableWrites[A, B]):
      TableWrites[Seq[A], Seq[B]] = new TableWrites[Seq[A], Seq[B]] {
    def writes(ts: Seq[A]) = ts.foldLeft(Seq.empty[B]){
      (prev, o) => prev :+ fmt.writes(o)
    }
  }

  /** Serializer for Option. */
  implicit def OptionWrites[A, B](implicit fmt: TableWrites[A, B]):
      TableWrites[Option[A], Option[B]] = new TableWrites[Option[A], Option[B]] {
    def writes(o: Option[A]) = o match {
      case Some(value) => Some(fmt.writes(value))
      case None        => None
    }
  }
}
