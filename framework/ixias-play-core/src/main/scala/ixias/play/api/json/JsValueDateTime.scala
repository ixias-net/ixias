/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._

// JodaTime
//~~~~~~~~~~~
object JsValueDateTime {

  val fmt = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  /** JSON : 読み込みコンビネータ */
  implicit val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern(fmt))
    )
  )

  /** JSON : 書き込みコンビネータ */
  implicit val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString())
  }
}
