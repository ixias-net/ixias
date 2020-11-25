/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import play.api.mvc.QueryStringBindable
import ixias.play.api.mvc.QueryStringHelper

trait CursorBindable {

  // --[ Alias ]----------------------------------------------------------------
  val  Cursor = ixias.persistence.model.Cursor
  type Cursor = ixias.persistence.model.Cursor

  // -- [ QueryStringBindable ] ------------------------------------------------
  /**
   * QueryString binder for `Cursor`
   */
  case class queryStringBindableCursor(
    limitDefault: Int,   // The number of results: default value
    limitMax:     Int    // The number of results: maximum value
  ) extends QueryStringBindable[Cursor] with QueryStringHelper {

    /**
     * Unbind a query string parameter.
     */
    def unbind(key: String, value: Cursor): String =
      Seq("offset" -> Option(value.offset), "limit" -> value.limit)
        .collect({ case (key, Some(v)) if v > 0 => key -> v })
        .map(item => "%s=%d".format(item._1, item._2))
        .mkString("&")

    /**
     * Bind a query string parameter.
     */
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Cursor]] = {
      implicit val _params = params
      (for {
        v1 <- implicitly[QueryStringBindable[Long]]._bindOption("offset")
        v2 <- implicitly[QueryStringBindable[Long]]._bindOption("limit")
      } yield v1 -> v2 match {
        case (Some(v1), Some(v2)) => Some(Cursor(v1, Some(v2)))
        case (None,     Some(v2)) => Some(Cursor(0L, Some(v2)))
        case (Some(v1), None)     => Some(Cursor(v1, Some(limitDefault.toLong)))
        case _                    => None
      }) match {
        case Left(v)          => Some(Left(v))
        case Right(None)      => None
        case Right(Some(cur)) => cur.limit.map(_ > limitMax) match {
          case Some(true) => Some(Right(cur.copy(limit = Some(limitMax.toLong))))
          case _          => Some(Right(cur))
        }
      }
    }
  }
}
