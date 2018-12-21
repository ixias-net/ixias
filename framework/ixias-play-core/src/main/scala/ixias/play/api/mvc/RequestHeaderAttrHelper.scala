/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.{ RequestHeader, Result }
import play.api.libs.typedmap.TypedKey


// Helper to get header attrs
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object RequestHeaderAttrHelper {
  import Errors._

  /** case Tuple1 */
  def get[T1](a1: TypedKey[T1])
    (implicit rh: RequestHeader): Either[Result, T1] =
    rh.attrs.get(a1) match {
      case Some(v1) => Right(v1)
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple2 */
  def get[T1, T2](a1: TypedKey[T1], a2: TypedKey[T2])
    (implicit rh: RequestHeader): Either[Result, (T1, T2)] =
    (rh.attrs.get(a1), rh.attrs.get(a2)) match {
      case (Some(v1), Some(v2)) => Right((v1, v2))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple3 */
  def get[T1, T2, T3](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3])
    (implicit rh: RequestHeader): Either[Result, (T1, T2, T3)] =
    (rh.attrs.get(a1), rh.attrs.get(a2), rh.attrs.get(a3)) match {
      case (Some(v1), Some(v2), Some(v3)) => Right((v1, v2, v3))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple4 */
  def get[T1, T2, T3, T4](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4])
    (implicit rh: RequestHeader): Either[Result, (T1, T2, T3, T4)] =
    (rh.attrs.get(a1), rh.attrs.get(a2), rh.attrs.get(a3), rh.attrs.get(a4)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4)) => Right((v1, v2, v3, v4))
      case _ => Left(E_NOT_FOUND)
    }

  /** case Tuple5 */
  def get[T1, T2, T3, T4, T5](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4], a5: TypedKey[T5])
    (implicit rh: RequestHeader): Either[Result, (T1, T2, T3, T4, T5)] =
    (rh.attrs.get(a1), rh.attrs.get(a2), rh.attrs.get(a3), rh.attrs.get(a4), rh.attrs.get(a5)) match {
      case (Some(v1), Some(v2), Some(v3), Some(v4), Some(v5)) => Right((v1, v2, v3, v4, v5))
      case _ => Left(E_NOT_FOUND)
    }
}
