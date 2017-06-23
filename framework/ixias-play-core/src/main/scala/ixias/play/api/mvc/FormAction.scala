/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc.Request
import play.api.data.{ Form, Mapping }

// Helper for HTTP-POST data
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
object FormAction {

  /**
   * To bind request data to a `T` component.
   */
  def bindFromRequest[T](mapping: Mapping[T])(implicit request: Request[_]): Either[Form[T], T] =
    Form(mapping).bindFromRequest.fold(Left(_), Right(_))
}
