/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import play.api.libs.json._

/**
 * Clound front URL
 */
case class CloudFrontUrl(
  meta:       File#EmbeddedId,
  width:      Option[Int] = None,
  height:     Option[Int] = None,
  responsive: Boolean     = false,
  format:     Option[UrlSigner.Request.Format] = None,
  custom:     Seq[(String, String)]            = Nil
)(implicit val dsn: ixias.aws.s3.backend.DataSourceName) {
  import UrlSigner.Request.Ratio

  /**
   * Generate request of file resizing
   */
  lazy val genUrlSignerRequest: Seq[UrlSigner.Request] =
    responsive match {
      case false => Seq(UrlSigner.Request(width, height, None, format, custom))
      case true  => Seq(
        UrlSigner.Request(width, height, Some(Ratio.IS_1x), format, custom),
        UrlSigner.Request(width, height, Some(Ratio.IS_2x), format, custom)
      )
    }
}

// The companion object
//~~~~~~~~~~~~~~~~~~~~~~
object CloudFrontUrl {
  import UrlSigner.Request.Ratio._
  import UrlSigner.getSigendCloudFrontUrl

  /**
   * Serializer for CloudFrontUrl
   */
  implicit object writes extends Writes[CloudFrontUrl] {
    def writes(data: CloudFrontUrl) = {
      implicit val dsn = data.dsn
      JsObject(Seq(
        Some("fid" -> JsNumber(data.meta.id)),
        Some("src" -> JsString(getSigendCloudFrontUrl(data.meta, data.genUrlSignerRequest.head).toString)),
        data.responsive match {
          case false => None
          case true  => Some("srcset" -> JsString(
            data.genUrlSignerRequest.map(
              resize => "%s %s".format(
                getSigendCloudFrontUrl(data.meta, resize)(data.dsn).toString,
                resize.ratio.getOrElse(IS_1x).value
              )
            ).mkString(", ")
          ))
        },
        data.width .map("width"  -> JsNumber(_)),
        data.height.map("height" -> JsNumber(_))
      ).flatten)
    }
  }
}

