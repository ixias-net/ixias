/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.data.{ Form, Mapping }
import play.api.mvc.{ Request, Result }
import play.api.mvc.Results.BadRequest
import play.api.i18n.MessagesProvider

// Helper for HTTP-POST data
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
object FormHelper {

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T](mapping: Mapping[T])(implicit
    req:      Request[_],
    provider: MessagesProvider
  ): Either[Result, T] =
    Form(mapping).bindFromRequest().fold(
      f => Left(BadRequest(f.errorsAsJson)),
      v => Right(v)
    )
}
