/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

import scala.reflect.ClassTag
import scala.collection.immutable.ListMap

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
  /** Convert to Same Type. */
  implicit def SameTypeConv[A <: AnyVal]: Converter[A, A] = new Converter[A, A] {
    def convert(v: A) = v
  }

  /** Serializer for Option. */
  implicit def OptionConv[A, B](implicit fmt: Converter[A, B]): Converter[Option[A], Option[B]] =
    new Converter[Option[A], Option[B]] {
      def convert(v: Option[A]) = v match {
        case Some(value) => Some(fmt.convert(value))
        case None        => None
      }
    }

  /** Serializer for Seq[T] types. */
  implicit def SeqConv[A: ClassTag, B: ClassTag](implicit fmt: Converter[A, B]): Converter[Seq[A], Seq[B]] =
    new Converter[Seq[A], Seq[B]] {
      def convert(itr: Seq[A]) = itr.foldLeft(Seq.empty[B]){
        (prev, v) => prev :+ fmt.convert(v)
      }
    }

  /**
   * Serializer for Map[String, T] types.
   */
  implicit def MapConv[A, B](implicit fmt: Converter[A, B]): Converter[Map[String, A], Map[String, B]] =
    new Converter[Map[String, A], Map[String, B]] {
      def convert(itr: Map[String, A]) = itr.foldLeft(ListMap.empty[String, B]){
        case (prev, (k, v)) => prev + (k -> fmt.convert(v))
      }
    }
}
