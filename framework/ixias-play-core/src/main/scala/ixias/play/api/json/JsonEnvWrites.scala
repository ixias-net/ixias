/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import play.api.libs.json._
import play.api.libs.json.EnvWrites

/**
 * Writes Conbinator for type conversion in service
 */
trait JsonEnvWrites extends EnvWrites {

  /**
   * Serializer for ixias.util.EnumStatus
   */
  implicit object EnumStatusWrites extends Writes[ixias.util.EnumStatus] {
    def writes(enum: ixias.util.EnumStatus) =
      JsNumber(enum.code)
  }

  /**
   * Serializer for Seq[ixias.util.EnumBitFlags]
   */
  implicit object EnumBitFlagsWrites extends Writes[ixias.util.EnumBitFlags] {
    def writes(enum: ixias.util.EnumBitFlags) =
      JsNumber(enum.code)
  }

  /**
   * Serializer for java.time.YearMonth
   */
  implicit object YearMonthWrites extends Writes[java.time.YearMonth] {
    def writes(yearMonth: java.time.YearMonth) =
      JsObject(Seq(
        "year"  -> JsNumber(yearMonth.getYear),
        "month" -> JsNumber(yearMonth.getMonthValue),
        "text"  -> JsString(yearMonth.format(
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        ))
      ))
  }
}
