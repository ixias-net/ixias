/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

import ixias.util.Enum
import ixias.aws.s3.backend.{ AmazonS3Config, DataSourceName }

import com.amazonaws.util.DateUtils
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner
import com.amazonaws.services.cloudfront.util.SignerUtils._

/**
 * The file resource definition
 * to provide clients with a URL to display the image
 */
object UrlSigner extends AmazonS3Config {

  protected val CF_CLOUD_FRONT_KEY_PAIR_ID         = "cloudfront_key_pair_id"
  protected val CF_CLOUD_FRONT_PRIVATE_KEY_FILE    = "cloudfront_private_key_file"
  protected val CF_CLOUD_FRONT_DISTRIBUTION_DOMAIN = "cloudfront_distribution_domain"
  protected val CF_CLOUD_FRONT_SIGNED_URL_TIMEOUT  = "cloudfront_signed_url_timeout"

  /**
   * The request to resize image.
   */
  case class Request(
    width:  Option[Int],
    height: Option[Int],
    ratio:  Option[Request.Ratio],
    format: Option[Request.Format],
    custom: Seq[(String, String)]
  ) {

    /**
     * Generate a URL query string.
     */
    lazy val queryString = (Seq(
      width  .map(v => "dw=%d"   .format(v)),
      height .map(v => "dh=%d"   .format(v)),
      ratio  .map(v => "ratio=%s".format(v.value)),
      format .map(v => "fmt=%s"  .format(v.value)),
    ).flatten ++
      custom .map(v => "%s=%s"   .format(v._1, v._2))
    ).mkString("&")
  }

  /**
   * Companion object: Resize-Request
   */
  object Request {

    // --[ Enum: Ratio ]----------------------------------------------------------
    sealed abstract class Ratio(val value: String) extends Enum
    object Ratio extends Enum.Of[Ratio] {
      case object IS_1x extends Ratio(value = "1x")
      case object IS_2x extends Ratio(value = "2x")
      case object IS_3x extends Ratio(value = "3x")
    }

    // --[ Enum: Format ]---------------------------------------------------------
    sealed abstract class Format(val value: String) extends Enum
    object Format extends Enum.Of[Format] {
      case object IS_JPEG extends Format(value = "jpeg")
      case object IS_PNG  extends Format(value = "png")
      case object IS_WEBP extends Format(value = "webp")
    }
  }

  /**
   * Implicit convertor: To java.time.Duration
   */
  implicit def toFiniteDuration(d: FiniteDuration): java.time.Duration =
    java.time.Duration.ofNanos(d.toNanos)

  /**
   * Generate a signed URL that allows access to distribution and S3 objects by
   * applying access restrictions specified in a custom policy document.
   */
  def getSigendCloudFrontUrl(file: File#EmbeddedId, resize: Request)
    (implicit dsn: DataSourceName): java.net.URL = {
      val keyPairId  = readValue(_.get[Option[String]](CF_CLOUD_FRONT_KEY_PAIR_ID)).get
      val pkFilePath = readValue(_.get[Option[String]](CF_CLOUD_FRONT_PRIVATE_KEY_FILE)).get
      val domain     = readValue(_.get[Option[String]](CF_CLOUD_FRONT_DISTRIBUTION_DOMAIN)).get
      val timeout    = readValue(_.get[Option[FiniteDuration]](CF_CLOUD_FRONT_SIGNED_URL_TIMEOUT))
                                   .getOrElse(FiniteDuration(30, TimeUnit.MINUTES))
      //- Generate Signed-URL
      val resourcePath = generateResourcePath(Protocol.https, domain, file.v.key)
      new java.net.URL({
        CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
          resourcePath + "?" + resize.queryString,
          keyPairId,
          loadPrivateKey(new java.io.File(pkFilePath)),
          DateUtils.parseISO8601Date(
            LocalDateTime.now.plus(timeout)
              .atZone(ZoneOffset.of("+09:00"))
              .toInstant.toString
          )
        )
      })
  }
}
