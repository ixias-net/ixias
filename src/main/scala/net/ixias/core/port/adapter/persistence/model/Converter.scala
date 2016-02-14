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

/** The data converter. */
trait Converter[-A, B] {
  def convert(o: A): B
}

/** The factory object for converter. */
object Converter extends TableDefaultConverter {
  def apply[A, B](f: A => B): Converter[A, B] = new Converter[A, B] {
    def convert(o: A): B = f(o)
  }
}

/** Default Serializers. */
trait TableDefaultConverter {
  import scala.language.implicitConversions

  /** Serializer for Seq[T] types. */
  implicit def SeqConv[A: ClassTag, B: ClassTag](implicit fmt: Converter[A, B]):
      Converter[Seq[A], Seq[B]] = new Converter[Seq[A], Seq[B]] {
    def convert(itr: Seq[A]) = itr.foldLeft(Seq.empty[B]){
      (prev, o) => prev :+ fmt.convert(o)
    }
  }

  /** Serializer for Option. */
  implicit def OptionConv[A, B](implicit fmt: Converter[A, B]):
      Converter[Option[A], Option[B]] = new Converter[Option[A], Option[B]] {
    def convert(o: Option[A]) = o match {
      case Some(value) => Some(fmt.convert(value))
      case None        => None
    }
  }
}
