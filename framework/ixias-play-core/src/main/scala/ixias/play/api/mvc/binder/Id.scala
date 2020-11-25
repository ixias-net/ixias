/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import play.api.mvc.{ PathBindable, QueryStringBindable }

/**
 * Binder utility function for ID implemented in Tagged-Type
 *
 * <Usage >
 * type UserId = Long @@ UserModel
 * implicit val  pathBindForUserId =        pathBindableBoxId[UserId]
 * implicit val queryBindForUserId = queryStringBindableBoxId[UserId]
 */
trait IdBindable extends Box {

  // -- [ PathBindable ] -------------------------------------------------------
  /**
   * PathBindable: ixias.model.@@[Long, _]
   */
  def pathBindableBoxId[T <: ixias.model.@@[_, _]]
    (implicit ctag: reflect.ClassTag[T]): PathBindable[Box[T]] = {
    val Id = ixias.model.the[ixias.model.Identity[T]]
    new PathBindable.Parsing[Box[T]](
      (s: String) => {
        val id = ctag.runtimeClass match {
          case x if    classOf[Int].isAssignableFrom(x) => Id(s.toInt.asInstanceOf[T])
          case x if   classOf[Long].isAssignableFrom(x) => Id(s.toLong.asInstanceOf[T])
          case x if classOf[String].isAssignableFrom(x) => Id(s.asInstanceOf[T])
          case _ => throw new IllegalArgumentException(
            "Unsupported type of id-value: %s".format(ctag.runtimeClass)
          )
        }
        () => id
      },
      (v: Box[T]) => Id.unwrap(v).toString,
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.@@[_, _]\n %s".format(key, e)
      }
    )
  }

  // -- [ QueryStringBindable ] ------------------------------------------------
  /**
   * For ixias.model.@@[_, _]
   */
  def queryStringBindableBoxId[T <: ixias.model.@@[_, _]]
    (implicit ctag: reflect.ClassTag[T]): QueryStringBindable[Box[T]] = {
    val Id = ixias.model.the[ixias.model.Identity[T]]
    new QueryStringBindable.Parsing[Box[T]](
      (s: String) => {
        val id = ctag.runtimeClass match {
          case x if    classOf[Int].isAssignableFrom(x) => Id(s.toInt.asInstanceOf[T])
          case x if   classOf[Long].isAssignableFrom(x) => Id(s.toLong.asInstanceOf[T])
          case x if classOf[String].isAssignableFrom(x) => Id(s.asInstanceOf[T])
          case _ => throw new IllegalArgumentException(
            "Unsupported type of id-value: %s".format(ctag.runtimeClass)
          )
        }
        () => id
      },
      (v: Box[T]) => Id.unwrap(v).toString,
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.@@[_, _]\n %s".format(key, e)
      }
    )
  }

  /**
   * For ixias.model.@@[_, _]
   */
  def queryStringBindableBoxCsvId[T <: ixias.model.@@[_, _]]
    (implicit ctag: reflect.ClassTag[T]): QueryStringBindable[BoxCsv[T]] = {
    val Id = ixias.model.the[ixias.model.Identity[T]]
    new QueryStringBindable.Parsing[BoxCsv[T]](
      (s: String) => {
        val ids: Seq[T] = s.split(",").map(tok =>
          ctag.runtimeClass match {
            case x if    classOf[Int].isAssignableFrom(x) => Id(tok.toInt.asInstanceOf[T])
            case x if   classOf[Long].isAssignableFrom(x) => Id(tok.toLong.asInstanceOf[T])
            case x if classOf[String].isAssignableFrom(x) => Id(tok.asInstanceOf[T])
            case _ => throw new IllegalArgumentException(
              "Unsupported type of id-value: %s".format(ctag.runtimeClass)
            )
          }
        )
        () => ids
      },
      (v: BoxCsv[T]) => v.map(
        id => Id.unwrap(id).toString
      ).mkString(","),
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.@@[_, _]\n %s".format(key, e)
      }
    )
  }
}
