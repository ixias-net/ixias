/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.data.{ Form, Mapping }
import play.api.mvc.{ Request, Result }
import play.api.mvc.Results._
import play.api.i18n.MessagesProvider

// Helper for HTTP-POST data
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
object FormHelper {

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T](mapping: Mapping[T])
    (implicit request: Request[_], provider: MessagesProvider): Either[Result, T] =
    Form(mapping).bindFromRequest().fold(
      f => Left(BadRequest(f.errorsAsJson)),
      v => Right(v)
    )
}
