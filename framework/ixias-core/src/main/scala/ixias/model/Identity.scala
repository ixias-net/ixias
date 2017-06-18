/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import scala.language.implicitConversions

sealed abstract class Identity[+A] extends Serializable { self =>

  // --[ Methods ]--------------------------------------------------------------
  /** Returns the identity's value. */
  def get: A

  /** Returns the identity's value as string. */
  def toString(): String

  /** Returns true if the identity is $noneId, false otherwise. */
  def isEmpty: Boolean

  /** Returns false if the identity is $noneId, true otherwise. */
  final def nonEmpty: Boolean = isDefined

  /** Returns true if the identity is an instance of $someId, false otherwise. */
  final def isDefined: Boolean = !isEmpty

  // --[ Methods ]--------------------------------------------------------------
  /** Returns the identity's value if the identity is nonempty, otherwise
    * return the result of evaluating `default`. */
  @inline final def getOrElse[B >: A](default: => B): B =
    if (isEmpty) default else this.get

  /** Returns this $identity if it is nonempty,
    * otherwise return the result of evaluating `alternative`. */
  @inline final def orElse[B >: A](alternative: => Identity[B]): Identity[B] =
    if (isEmpty) alternative else this

  /** Returns the identity's value if it is nonempty, or `null` if it is empty.
    * Although the use of null is discouraged, code written to use
    * $identity must often interface with code that expects and returns nulls. */
  @inline final def orNull[A1 >: A](implicit ev: Null <:< A1): A1 = this getOrElse ev(null)

  // --[ Methods ]--------------------------------------------------------------
  /** Tests whether the identity contains a given value as an element. */
  final def contains[A1 >: A](elem: A1): Boolean =
    !isEmpty && this.get == elem

  /** Returns true if this identity is nonempty '''and''' the predicate
    * $p returns true when applied to this $identity's value. Otherwise, returns false. */
  @inline final def exists(p: A => Boolean): Boolean =
    !isEmpty && p(this.get)

  /** Returns true if this identity is empty '''or''' the predicate
    * $p returns true when applied to this $identity's value. */
  @inline final def forall(p: A => Boolean): Boolean = isEmpty || p(this.get)

  /** Apply the given procedure $f to the identity's value,
    * if it is nonempty. Otherwise, do nothing. */
  @inline final def foreach[U](f: A => U) {
    if (!isEmpty) f(this.get)
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Returns a $someId containing the result of applying $f to this $identity's
    * value if this $identity is nonempty. Otherwise return $noneId. */
  @inline final def map[B](f: A => B): Identity[B] =
    if (isEmpty) NoneId else SomeId(f(this.get))

  /** Returns the result of applying $f to this $identity's value
    * if the $identity is nonempty.  Otherwise, evaluates expression `ifEmpty`. */
  @inline final def fold[B](ifEmpty: => B)(f: A => B): B =
    if (isEmpty) ifEmpty else f(this.get)

  /** Returns the result of applying $f to this $identity's value
    * if this $identity is nonempty. Returns $noneId if this $identity is empty.*/
  @inline final def flatMap[B](f: A => Identity[B]): Identity[B] =
    if (isEmpty) NoneId else f(this.get)

  def flatten[B](implicit ev: A <:< Identity[B]): Identity[B] =
    if (isEmpty) NoneId else ev(this.get)

  /** Returns this $identity if it is nonempty '''and''' applying the predicate $p to
    * this $identity's value returns true. Otherwise, return $noneId. */
  @inline final def filter(p: A => Boolean): Identity[A] =
    if (isEmpty || p(this.get)) this else NoneId

  /** Returns this $identity if it is nonempty '''and''' applying the predicate $p to
    * this $identity's value returns false. Otherwise, return $noneId. */
  @inline final def filterNot(p: A => Boolean): Identity[A] =
    if (isEmpty || !p(this.get)) this else NoneId

  /** Necessary to keep $identity from being implicitly converted to
    *  [[scala.collection.Iterable]] in `for` comprehensions. */
  @inline final def withFilter(p: A => Boolean): WithFilter = new WithFilter(p)

  /** We need a whole WithFilter class to honor the "doesn't create a new
    * collection" contract even though it seems unlikely to matter much in a
    * collection with max size 1. */
  class WithFilter(p: A => Boolean) {
    def map[B](f: A => B): Identity[B] = self filter p map f
    def flatMap[B](f: A => Identity[B]): Identity[B] = self filter p flatMap f
    def foreach[U](f: A => U): Unit = self filter p foreach f
    def withFilter(q: A => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
  }

  // --[ Methods ]--------------------------------------------------------------
  /** Returns a [[scala.Option]] containing the $identity's value. */
  def toOption: Option[A] =
    if (isEmpty) None else Some(this.get)

  /** Returns a singleton list containing the $identity's value
    * if it is nonempty, or the empty list if the $identity is empty. */
  def toList: List[A] =
    if (isEmpty) List() else new ::(this.get, Nil)

  /** Returns a singleton iterator returning the $identity's value
    * if it is nonempty, or an empty iterator if the identity is empty. */
  def iterator: Iterator[A] =
    if (isEmpty) collection.Iterator.empty else collection.Iterator.single(this.get)

  /** Returns a [[scala.util.Left]] containing the given
    * argument `left` if this $identity is empty, or
    * a [[scala.util.Right]] containing this $identity's value if this is nonempty. */
  @inline final def toRight[X](left: => X) =
    if (isEmpty) Left(left) else Right(this.get)

  /** Returns a [[scala.util.Right]] containing the given
    * argument `right` if this is empty, or
    * a [[scala.util.Left]] containing this $identity's value if this $identity is nonempty. */
  @inline final def toLeft[X](right: => X) =
    if (isEmpty) Right(right) else Left(this.get)
}

object Identity
{
  /** An implicit conversion that converts an identity to an iterable value */
  implicit def identity2Iterable[A](xo: Identity[A]): Iterable[A] = xo.toList

  /** An Identity factory which creates SomeId(x) if the argument is not null,
    * and NoneId if it is null. */
  def apply[A](x: A): Identity[A] = if (x == null) NoneId else SomeId(x)

  /** An Identity factory which creates SomeId(x) if the argument is `Some`,
    * and NoneId if it is None. */
  def apply[A](x: Option[A]): Identity[A] = x.map(SomeId(_)).getOrElse(NoneId)

  /** An Identity factory which returns `NoneId`
    * in a manner consistent with the collections hierarchy. */
  def empty[A] : Identity[A] = NoneId

  /** @see `Tag.of` */
  final class TagOf[T]() {
    def apply[A](a: A):         Identity[A @@ T] = SomeId(Tag.apply(a))
    def apply[A](a: Option[A]): Identity[A @@ T] = a match {
      case Some(v) => SomeId(Tag.apply(v))
      case None    => NoneId.asInstanceOf[Identity[A @@ T]]
    }
    def empty[A]: Identity[A @@ T] = NoneId.asInstanceOf[Identity[A @@ T]]
  }
  def of[T]: TagOf[T] = new TagOf[T]
}

/**
 * Class `SomeId[A]` represents existing values of type `A`.
 */
final case class SomeId[+A](x: A) extends Identity[A]
{
  def isEmpty = false
  def get = x
}

/**
 * This case object represents non-existent values.
 */
case object NoneId extends Identity[Nothing]
{
  def isEmpty = true
  def get = throw new NoSuchElementException("NoneId.get")
}
