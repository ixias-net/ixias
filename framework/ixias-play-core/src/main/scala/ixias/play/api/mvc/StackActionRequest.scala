/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc._
import scala.concurrent.Future
import scala.collection.concurrent.TrieMap
import ixias.play.api.mvc.Errors._

// Wrap an existing request. Useful to extend a request.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
import StackActionRequest._
case class StackActionRequest[A](
  underlying: Request[A],
  attributes: TrieMap[AttributeKey[_], Any] = TrieMap.empty
) extends WrappedRequest[A](underlying) {

  /**
   * Retrieve an attribute by specific key.
   */
  def get[B](key: AttributeKey[B]): Option[B] =
    attributes.get(key).asInstanceOf[Option[B]]

  /**
   * Store an attribute under the specific key.
   */
  def set[B](key: AttributeKey[B], value: B): StackActionRequest[A] = {
    attributes.put(key, value)
    this
  }

  /**
   * Store an attributes
   */
  def ++=[B](tail: TrieMap[AttributeKey[_], Any]): StackActionRequest[A] = {
    attributes ++= tail
    this
  }
}

// The declaration for request's attribute.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object StackActionRequest {

  /**
   * The attribute key of request.
   */
  trait AttributeKey[A] {
    def ->(value: A): Attribute[A] = Attribute(this, value)
  }

  /**
   * The attribute of request.
   */
  case class Attribute[A](key: AttributeKey[A], value: A) {
    def toTuple: (AttributeKey[A], A) = (key, value)
  }
}

// The helper to retrieve request data.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object StackAction {

  /** case Tuple1 */
  def bindFromRequest[T1]
    (a1: AttributeKey[T1])
    (block: (T1) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    r.get(a1) match {
      case Some(v1) => block(v1)
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple2 */
  def bindFromRequest[T1, T2]
    (a1: AttributeKey[T1], a2: AttributeKey[T2])
    (block: ((T1, T2)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2)) match {
      case (Some(v1), Some(v2)) => block((v1, v2))
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple3 */
  def bindFromRequest[T1, T2, T3]
    (a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3])
    (block: ((T1, T2, T3)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2), r.get(a3)) match {
      case (Some(v1), Some(v2), Some(v3)) => block((v1, v2, v3))
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple4 */
  def bindFromRequest[T1, T2, T3, T4]
    (a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4])
    (block: ((T1, T2, T3, T4)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4)) => block((v1, v2, v3, v4))
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5]
    (a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5])
    (block: ((T1, T2, T3, T4, T5)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5)) => block((v1, v2, v3, v4, v5))
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5, T6]
    (a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5], a6: AttributeKey[T6])
    (block: ((T1, T2, T3, T4, T5, T6)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5), r.get(a6)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5), Some(v6)) => block((v1, v2, v3, v4, v5, v6))
      case _ => Future.successful(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5, T6, T7]
    (a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5], a6: AttributeKey[T6], a7: AttributeKey[T7])
    (block: ((T1, T2, T3, T4, T5, T6, T7)) => Future[Result])(implicit r: StackActionRequest[_]): Future[Result] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5), r.get(a6), r.get(a7)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5), Some(v6), Some(v7)) => block((v1, v2, v3, v4, v5, v6, v7))
      case _ => Future.successful(E_NOT_FOUND)
    }
}
