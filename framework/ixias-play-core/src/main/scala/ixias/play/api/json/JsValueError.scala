/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import play.api.libs.json._
import play.api.libs.functional.syntax._

// The Error response
//~~~~~~~~~~~~~~~~~~~~
case class JsValueError(
  val error:   Int,            // Error code
  val message: Option[String]  // Error message
)

object JsValueError {
  implicit val writes: Writes[JsValueError] = (
    (__ \ "error"  ).write[Int] and
    (__ \ "message").write[Option[String]]
  )(unlift(JsValueError.unapply))
}
