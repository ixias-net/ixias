/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import cats.Applicative
import cats.data.{ Validated, ValidatedNel, NonEmptyList }
import scala.reflect.runtime.universe._
import scala.language.implicitConversions

import play.api.mvc.{ RequestHeader, Result }
import play.api.libs.typedmap.TypedKey
import ixias.util.Logging

// Helper to get header attrs
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object RequestHeaderAttrHelper extends Logging {
  import Errors._

  type ErrorOr[A] = ValidatedNel[String, A]

  /**
   * Retrieves and validate a value of the specified key.
   */
  def getValue[T](key: TypedKey[T])(implicit rh: RequestHeader, tag: TypeTag[T]): ValidatedNel[String, T] =
    rh.attrs.get(key) match {
      case Some(v) => Validated.Valid(v)
      case None    => {
        Validated.Invalid(NonEmptyList.of(
          "The value under the specified key was not found. Entity type is "
            + tag.tpe.toString
        ))
      }
    }

  /**
   * Implicit convert. ValidatedNel to Either
   */
  implicit def toEither[T](validated: ValidatedNel[String, T]): Either[Result, T] =
    validated match {
      case Validated.Valid(v)     => Right(v)
      case Validated.Invalid(nel) => {
        nel.map(invalid => logger.error(invalid))
        Left(E_NOT_FOUND)
      }
    }

  /** case Tuple1 */
  def get[T1](a1: TypedKey[T1])
    (implicit rh: RequestHeader, tag1: TypeTag[T1]):
      Either[Result, T1] =
    getValue(a1)


  /** case Tuple2 */
  def get[T1, T2](a1: TypedKey[T1], a2: TypedKey[T2])
    (implicit rh: RequestHeader, tag1: TypeTag[T1], tag2: TypeTag[T2]):
      Either[Result, (T1, T2)] =
    Applicative[ErrorOr].map2(
      getValue(a1),
      getValue(a2)
    )((_, _))

  /** case Tuple3 */
  def get[T1, T2, T3](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3])
    (implicit rh: RequestHeader, tag1: TypeTag[T1], tag2: TypeTag[T2], tag3: TypeTag[T3]):
      Either[Result, (T1, T2, T3)] =
    Applicative[ErrorOr].map3(
      getValue(a1),
      getValue(a2),
      getValue(a3)
    )((_, _, _))

  /** case Tuple4 */
  def get[T1, T2, T3, T4](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4])
    (implicit rh: RequestHeader, tag1: TypeTag[T1], tag2: TypeTag[T2], tag3: TypeTag[T3], tag4: TypeTag[T4]):
      Either[Result, (T1, T2, T3, T4)] =
    Applicative[ErrorOr].map4(
      getValue(a1),
      getValue(a2),
      getValue(a3),
      getValue(a4)
    )((_, _, _, _))

  /** case Tuple5 */
  def get[T1, T2, T3, T4, T5](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4], a5: TypedKey[T5])
    (implicit rh: RequestHeader, tag1: TypeTag[T1], tag2: TypeTag[T2], tag3: TypeTag[T3], tag4: TypeTag[T4], tag5: TypeTag[T5]):
      Either[Result, (T1, T2, T3, T4, T5)] =
    Applicative[ErrorOr].map5(
      getValue(a1),
      getValue(a2),
      getValue(a3),
      getValue(a4),
      getValue(a5)
    )((_, _, _, _, _))
}
