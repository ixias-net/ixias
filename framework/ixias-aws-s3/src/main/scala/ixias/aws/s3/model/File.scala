/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model


import scala.util.Try
import java.time.LocalDateTime
import java.time.temporal.ChronoField._
import com.amazonaws.Protocol
import com.amazonaws.services.s3.model.S3Object
import ixias.model._
import ixias.aws.s3.backend.{ AmazonS3Config, DataSourceName }

// The file representation for Amazon S3.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class File(
  val id:           Option[File.Id],             // Id
  val region:       String,                      // AWS region
  val bucket:       String,                      // The bucket of S3
  val key:          String,                      // The file key.
  val typedef:      String,                      // The file-type.
  val imageSize:    Option[File.ImageSize],      // If file-type is image. image size is setted.
  val presignedUrl: Option[java.net.URL] = None, // The presigned Url to accessing on Image
  val updatedAt:    LocalDateTime        = NOW,  // The Datetime when a data was updated.
  val createdAt:    LocalDateTime        = NOW   // The Datetime when a data was created.
) extends EntityModel[File.Id] {

  lazy val httpsUrl       = s"${Protocol.HTTPS.toString()}://${httpsUrn}"
  lazy val httpsUrlOrigin = s"${Protocol.HTTPS.toString()}://${httpsUrnOrigin}"
  lazy val httpsUrn       = s"cdn-${bucket}/${key}?d=${(updatedAt.get(MILLI_OF_SECOND)/1000).toHexString}&${presignedQuery}"
  lazy val httpsUrnOrigin = s"s3-${region}.amazonaws.com/${bucket}/${key}?${presignedQuery}"
  lazy val presignedQuery = presignedUrl.map(v => v.getQuery).getOrElse("")

  /** Build a empty S3 object. */
  def emptyS3Object: S3Object = {
    val s3object = new S3Object
    s3object.setBucketName(bucket)
    s3object.setKey(key)
    s3object
  }
}

// The companion object
//~~~~~~~~~~~~~~~~~~~~~~
object File {

  // --[ File ID ]--------------------------------------------------------------
  val  Id         = the[Identity[Id]]
  type Id         = Long @@ File
  type WithNoId   = Entity.WithNoId   [Id, File]
  type EmbeddedId = Entity.EmbeddedId [Id, File]

  // --[ Type Alias ]-----------------------------------------------------------
  type    Silo    [K  <: @@[Long, _]]                                = FileSilo[K]
  type Builder    [S  <: @@[Long, _], M  <: AnyRef]                  = FileBuilder[S, M]
  type BuilderExt [S1 <: @@[Long, _], S2 <: @@[Long, _],M <: AnyRef] = FileBuilderExt[S1, S2, M]

  object Config extends AmazonS3Config

  // --[ Create a new object ]--------------------------------------------------
  def apply(key: String, typedef: String, size: Option[ImageSize])
    (implicit dns: DataSourceName): Try[Entity.WithNoId[File.Id, File]] =
    for {
      region <- Config.getAWSRegion
      bucket <- Config.getBucketName
    } yield Entity.WithNoId[File.Id, File](
      new File(None, region.getName, bucket, key, typedef, size)
    )

  // --[ The iamage size ]------------------------------------------------------
  case class ImageSize(width: Int, height: Int) {

    /** The aspect ration */
    val aspectRatio: Float = (BigDecimal(width) / BigDecimal(height))
      .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
      .toFloat

    /** Change image size with keep aspect ratio. */
    def changeScale(rate: Float): ImageSize =
      this.copy(
        width  = ( width.toFloat * rate).toInt,
        height = (height.toFloat * rate).toInt
      )

    /** Change image width size with keep aspect ratio. */
    def changeWidthSize(width: Int): ImageSize = {
      val rate = (BigDecimal(width) / BigDecimal(this.width))
        .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
        .toFloat
      this.copy(width = width, height = (height.toFloat * rate).toInt)
    }

    /** Change image height size with keep aspect ratio. */
    def changeHeightSize(height: Int): ImageSize = {
      val rate = (BigDecimal(height) / BigDecimal(this.height))
        .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
        .toFloat
      this.copy(width = (width.toFloat * rate).toInt, height = height)
    }
  }
}

