/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Results.BadRequest
import play.api.mvc.{ QueryStringBindable, Result }
import scala.language.implicitConversions

/**
 * Extending `QueryStringBindable`
 */
trait QueryStringHelper extends binder.Box {

  implicit def toQueryStringHelperOps[A](bindable: QueryStringBindable[A]) =
    QueryStringHelper.QueryStringHelperOps(bindable)

  implicit def toQueryStringHelperBoxCsvOps[A](bindable: QueryStringBindable[BoxCsv[A]]) =
    QueryStringHelper.QueryStringHelperBoxCsvOps(bindable)
}

/**
 * Companion object
 */
object QueryStringHelper extends binder.Box {

  /**
   * Operations extending `QueryStringBindable`
   */
  case class QueryStringHelperOps[A](val bindable: QueryStringBindable[A]) extends AnyVal {

    /**
     * Get a value from queryString parameter.
     */
    def bindOption(key: String)
      (implicit params: Map[String, Seq[String]]): Either[Result, Option[A]] =
      _bindOption(key).left.map(BadRequest(_))

    /**
     * Get a value from queryString parameter.
     */
    private[mvc] def _bindOption(key: String)
      (implicit params: Map[String, Seq[String]]): Either[String, Option[A]] =
      bindable.bind(key, params) match {
        case None           => Right(None)
        case Some(Right(v)) => Right(Some(v))
        case Some(Left(v))  => Left(v)
      }
  }

  /**
   * Operations extending `QueryStringBindable`
   */
  case class QueryStringHelperBoxCsvOps[A](val bindable: QueryStringBindable[BoxCsv[A]]) extends AnyVal {

    /**
     * Get a value from queryString parameter.
     */
    def bind(key: String)
      (implicit params: Map[String, Seq[String]]): Either[Result, Seq[A]] =
      _bind(key).left.map(BadRequest(_))

    /**
     * Get a value from queryString parameter.
     */
    private[mvc] def _bind(key: String)
      (implicit params: Map[String, Seq[String]]): Either[String, Seq[A]] =
      bindable.bind(key, params) match {
        case None           => Right(Nil)
        case Some(Right(v)) => Right(v())
        case Some(Left(v))  => Left(v)
      }
  }
}
