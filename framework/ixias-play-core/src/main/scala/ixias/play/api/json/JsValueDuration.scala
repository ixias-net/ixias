/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import org.joda.time.Duration
import play.api.libs.json._
import play.api.libs.functional.syntax._

// JodaTime JSON Formatter
//~~~~~~~~~~~~~~~~~~~~~~~~~
object JsValueDuration {

  /**
   * Deserializer of Joda Duration,
   * from either a time-based amount of time or from a number of milliseconds
   */
  implicit val jodaDurationReads: Reads[Duration] = Reads[Duration] {
    case JsString(repr) => try {
      JsSuccess(Duration.parse(repr))
    } catch {
      case _: IllegalArgumentException => JsError("error.invalid.duration")
    }
    case js => Reads[Duration] {
      case JsNumber(n) if !n.ulp.isValidLong => JsError("error.invalid.longDuration")
      case JsNumber(n) => JsSuccess(new Duration(n.toLong))
      case _           => JsError("error.expected.lonDuration")
    } reads(js)
  }

  /**
   * Serializer of Joda Duration using ISO representation
   */
  implicit val jodaDurationWrites: Writes[Duration] =
    Writes[Duration] { d => JsString(d.toString) }
}
