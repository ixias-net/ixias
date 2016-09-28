/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Result
import scala.concurrent.Future
import ixias.play.api.mvc.Errors._
import StackActionRequest._

// The helper to retrieve request data.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object StackAction {

  /** case Tuple1 */
  def bindFromRequest[T1](a1: AttributeKey[T1])
    (implicit r: StackActionRequest[_]): Either[Result, T1] =
    r.get(a1) match {
      case Some(v1) => Right(v1)
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple2 */
  def bindFromRequest[T1, T2](a1: AttributeKey[T1], a2: AttributeKey[T2])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2)] =
    (r.get(a1), r.get(a2)) match {
      case (Some(v1), Some(v2)) => Right((v1, v2))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple3 */
  def bindFromRequest[T1, T2, T3](a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2, T3)] =
    (r.get(a1), r.get(a2), r.get(a3)) match {
      case (Some(v1), Some(v2), Some(v3)) => Right((v1, v2, v3))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple4 */
  def bindFromRequest[T1, T2, T3, T4](a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2, T3, T4)] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4)) => Right((v1, v2, v3, v4))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5](a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2, T3, T4, T5)] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5)) => Right((v1, v2, v3, v4, v5))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5, T6](a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5], a6: AttributeKey[T6])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2, T3, T4, T5, T6)] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5), r.get(a6)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5), Some(v6)) => Right((v1, v2, v3, v4, v5, v6))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def bindFromRequest[T1, T2, T3, T4, T5, T6, T7](a1: AttributeKey[T1], a2: AttributeKey[T2], a3: AttributeKey[T3], a4: AttributeKey[T4], a5: AttributeKey[T5], a6: AttributeKey[T6], a7: AttributeKey[T7])
    (implicit r: StackActionRequest[_]): Either[Result, (T1, T2, T3, T4, T5, T6, T7)] =
    (r.get(a1), r.get(a2), r.get(a3), r.get(a4), r.get(a5), r.get(a6), r.get(a7)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5), Some(v6), Some(v7)) => Right((v1, v2, v3, v4, v5, v6, v7))
      case _ => Left(E_NOT_FOUND)
    }
}
