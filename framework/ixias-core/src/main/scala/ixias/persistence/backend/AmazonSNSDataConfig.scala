/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import com.amazonaws.regions.Regions
import com.amazonaws.auth.{ AWSCredentials, BasicAWSCredentials }
import ixias.util.Configuration
import ixias.persistence.model.DataSourceName

trait AmazonSNSDataConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_ACCESS_KEY        = "aws_access_key_id"
  protected val CF_SECRET_KEY        = "aws_secret_access_key"
  protected val CF_REGION            = "aws_region"
  protected val CF_OPT_SNS_SKIP      = "aws_sns_skip"
  protected val CF_OPT_SNS_TOPIC_ARN = "aws_sns_topic"

  /** The configuration */
  protected val config = Configuration()

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Gets the AWS credentials object.
   */
  protected def getAWSCredentials(dsn: DataSourceName): Try[AWSCredentials] =
    for {
      akey <- getAWSAccessKeyId(dsn)
      skey <- getAWSSecretKey(dsn)
    } yield new BasicAWSCredentials(akey, skey)

  /**
   * Gets the AWS access key ID for this credentials object.
   */
  protected def getAWSAccessKeyId(dsn: DataSourceName): Try[String] =
    Try(readValue(dsn)(_.getString(CF_ACCESS_KEY)).get)

  /**
   * Gets the AWS secret access key for this credentials object.
   */
  protected def getAWSSecretKey(dsn: DataSourceName): Try[String] =
    Try(readValue(dsn)(_.getString(CF_SECRET_KEY)).get)

  /**
   * Gets a region enum corresponding to the given region name.
   */
  protected def getAWSRegion(dsn: DataSourceName): Try[Regions] =
    Try(Regions.fromName(readValue(dsn)(_.getString(CF_REGION)).get))

  /**
   * Gets the flag to invoke SNS process.
   */
  def isSkip(dsn: DataSourceName): Boolean =
    readValue(dsn)(_.getBoolean(CF_OPT_SNS_SKIP)).getOrElse(false)

  /**
   * Gets the topic ARN of Amazon SNS.
   */
  def getTopicARN(dsn: DataSourceName): Try[Seq[String]] =
    Try {
      import scala.collection.JavaConverters._
      val path = dsn.path + "." + dsn.database
      config.getConfig(path).map { section =>
        section.underlying.getAnyRef(CF_OPT_SNS_TOPIC_ARN) match {
          case v: String            => Seq(v)
          case v: java.util.List[_] => asScalaBufferConverter(v).asScala.toList.map(_.toString)
          case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
        }
      }.get
    }

  // --[ Configuration ]--------------------------------------------------------
  /**
   * Get a value by specified key.
   */
  protected def readValue[A](dsn: DataSourceName)(f: Configuration => Option[A]): Option[A] =
    Seq(
      dsn.path + "." + dsn.database,
      dsn.path
    ).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse(config.getConfig(path).flatMap(f))
    }
}
