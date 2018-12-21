/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.sns.backend

import scala.util.Try
import scala.collection.JavaConverters._
import com.amazonaws.regions.Regions
import com.amazonaws.auth.{ AWSCredentials, BasicAWSCredentials }
import ixias.util.Configuration

trait AmazonSNSConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_SNS_ACCESS_KEY        = "access_key_id"
  protected val CF_SNS_SECRET_KEY        = "secret_access_key"
  protected val CF_SNS_REGION            = "region"
  protected val CF_SNS_OPT_SNS_SKIP      = "skip"
  protected val CF_SNS_OPT_SNS_TOPIC_ARN = "topic"

  /** The configuration */
  protected val config = Configuration()

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the AWS credentials object.
   */
  protected def getAWSCredentials(implicit dsn: DataSourceName): Try[AWSCredentials] =
    for {
      akey <- getAWSAccessKeyId
      skey <- getAWSSecretKey
    } yield new BasicAWSCredentials(akey, skey)

  /**
   * Gets the AWS access key ID for this credentials object.
   */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_SNS_ACCESS_KEY)).get
    )

  /**
   * Gets the AWS secret access key for this credentials object.
   */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(
      _.get[Option[String]](CF_SNS_SECRET_KEY)).get
    )

  /**
   * Gets a region enum corresponding to the given region name.
   */
  protected def getAWSRegion(implicit dsn: DataSourceName): Try[Regions] =
    Try(Regions.fromName(readValue(
      _.get[Option[String]](CF_SNS_REGION)).get
    ))

  /**
   * Gets the flag to invoke SNS process.
   */
  def isSkip(implicit dsn: DataSourceName): Boolean =
    readValue(
      _.get[Option[Boolean]](CF_SNS_OPT_SNS_SKIP)
    ).getOrElse(false)

  /**
   * Gets the topic ARN of Amazon SNS.
   */
  def getTopicARN(implicit dsn: DataSourceName): Try[Seq[String]] = {
    val path = dsn.name match {
      case None       => dsn.path + "." + dsn.resource
      case Some(name) => dsn.path + "." + dsn.resource + "." + name
    }
    val section = config.get[Configuration](path).underlying
    val opt     = section.getAnyRef(CF_SNS_OPT_SNS_TOPIC_ARN) match {
      case v: String            => Seq(v)
      case v: java.util.List[_] => v.asScala.toList.map(_.toString)
      case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
    }
    Try(opt)
  }

  /**
   * Get a value by specified key.
   */
  final private def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    (dsn.name.toSeq.map(
      name => dsn.path + "." + dsn.resource + "." + name
    ) ++ Seq(
      dsn.path + "." + dsn.resource,
      dsn.path
    )).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse(f(config.get[Configuration](path)))
    }
}
