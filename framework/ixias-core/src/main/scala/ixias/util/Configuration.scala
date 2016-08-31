/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.util.control.NonFatal

import com.typesafe.config._
import com.typesafe.config.impl.ConfigImpl

/**
 * A full configuration set.
 */
case class Configuration(underlying: Config = ConfigFactory.load()) {

  /**
   * Merge 2 configurations.
   */
  def ++(other: Configuration): Configuration = {
    Configuration(other.underlying.withFallback(underlying))
  }

  /**
   * Reads a value from the underlying implementation.
   * If the value is not set this will return None, otherwise returns Some.
   */
  private def readValue[T](path: String, v: => T): Option[T] =
    if (underlying.hasPathOrNull(path)) Some(v) else None


  /**
   * Retrieves a configuration value as a `String`.
   */
  def getString(path: String, validValues: Option[Set[String]] = None): Option[String] =
    readValue(path, underlying.getString(path)).map { value =>
      validValues match {
        case Some(values) if values.contains(value) => value
        case Some(values) if values.isEmpty => value
        case Some(values) => throw new Exception(
          "%s::Incorrect value, one of %s was expected."
            .format(path, values.reduceLeft(_ + ", " + _)))
        case None => value
      }
    }

  /**
   * Retrieves a configuration value as an `Int`.
   */
  def getInt(path: String): Option[Int] =
    readValue(path, underlying.getInt(path))

  /**
   * Retrieves a configuration value as a `Boolean`.
   */
  def getBoolean(path: String): Option[Boolean] =
    readValue(path, underlying.getBoolean(path))

  /**
   * Retrieves a configuration value as `Milliseconds`.
   */
  def getMilliseconds(path: String): Option[Long] =
    readValue(path, underlying.getDuration(path, TimeUnit.MILLISECONDS))

  /**
   * Retrieves a configuration value as `Nanoseconds`.
   */
  def getNanoseconds(path: String): Option[Long] =
    readValue(path, underlying.getDuration(path, TimeUnit.NANOSECONDS))

  /**
   * Retrieves a configuration value as `Duration`.
   */
  def getDuration(path: String): Option[Duration] =
    getMilliseconds(path).map(FiniteDuration(_, TimeUnit.MILLISECONDS))

  /**
   * Retrieves a configuration value as `FiniteDuration`.
   */
  def getFiniteDuration(path: String): Option[FiniteDuration] =
    getMilliseconds(path).map(FiniteDuration(_, TimeUnit.MILLISECONDS))

  /**
   * Retrieves a configuration value as `Bytes`.
   */
  def getBytes(path: String): Option[Long] =
    readValue(path, underlying.getBytes(path))

  /**
   * Retrieves a sub-configuration
   */
  def getConfig(path: String): Option[Configuration] =
    readValue(path, underlying.getConfig(path)).map(Configuration(_))

  /**
   * Retrieves a configuration value as a `Double`.
   */
  def getDouble(path: String): Option[Double] =
    readValue(path, underlying.getDouble(path))

  /**
   * Retrieves a configuration value as a `Long`.
   */
  def getLong(path: String): Option[Long] =
    readValue(path, underlying.getLong(path))

  /**
   * Retrieves a configuration value as a `Number`.
   */
  def getNumber(path: String): Option[Number] =
    readValue(path, underlying.getNumber(path))

  /**
   * Retrieves a configuration value as a List of `Boolean`.
   * (ex) board.switches = [true, true, false]
   */
  def getBooleanList(path: String): Option[java.util.List[java.lang.Boolean]] =
    readValue(path, underlying.getBooleanList(path))

  /**
   * Retrieves a configuration value as a Seq of `Boolean`.
   * (ex) board.switches = [true, true, false]
   */
  def getBooleanSeq(path: String): Option[Seq[java.lang.Boolean]] =
    getBooleanList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as a List of `Bytes`.
   * (ex) engine.maxSizes = [512k, 256k, 256k]
   */
  def getBytesList(path: String): Option[java.util.List[java.lang.Long]] =
    readValue(path, underlying.getBytesList(path))

  /**
   * Retrieves a configuration value as a Seq of `Bytes`.
   * (ex) engine.maxSizes = [512k, 256k, 256k]
   */
  def getBytesSeq(path: String): Option[Seq[java.lang.Long]] =
    getBytesList(path).map(asScalaList)

  /**
   * Retrieves a List of sub-configurations
   */
  def getConfigList(path: String): Option[java.util.List[Configuration]] =
    readValue[java.util.List[_ <: Config]](path, underlying.getConfigList(path))
      .map { configs => configs.asScala.map(Configuration(_)).asJava }

  /**
   * Retrieves a Seq of sub-configurations
   */
  def getConfigSeq(path: String): Option[Seq[Configuration]] =
    getConfigList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as a List of `Double`.
   * engine.maxSizes = [5.0, 3.34, 2.6]
   */
  def getDoubleList(path: String): Option[java.util.List[java.lang.Double]] =
    readValue(path, underlying.getDoubleList(path))

  /**
   * Retrieves a configuration value as a Seq of `Double`.
   * (ex) engine.maxSizes = [5.0, 3.34, 2.6]
   */
  def getDoubleSeq(path: String): Option[Seq[java.lang.Double]] =
    getDoubleList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as a List of `Integer`.
   * (ex) engine.maxSizes = [100, 500, 2]
   */
  def getIntList(path: String): Option[java.util.List[java.lang.Integer]] =
    readValue(path, underlying.getIntList(path))

  /**
   * Retrieves a configuration value as a Seq of `Integer`.
   * (ex) engine.maxSizes = [100, 500, 2]
   */
  def getIntSeq(path: String): Option[Seq[java.lang.Integer]] =
    getIntList(path).map(asScalaList)

  /**
   * Gets a list value (with any element type) as a ConfigList,
   * which implements java.util.List<ConfigValue>.
   * (ex) engine.maxSizes = ["foo", "bar"]
   */
  def getList(path: String): Option[ConfigList] =
    readValue(path, underlying.getList(path))

  /**
   * Retrieves a configuration value as a List of `Long`.
   * (ex) engine.maxSizes = [10000000000000, 500, 2000]
   */
  def getLongList(path: String): Option[java.util.List[java.lang.Long]] =
    readValue(path, underlying.getLongList(path))

  /**
   * Retrieves a configuration value as a Seq of `Long`.
   * (ex) engine.maxSizes = [10000000000000, 500, 2000]
   */
  def getLongSeq(path: String): Option[Seq[java.lang.Long]] =
    getLongList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as List of `Milliseconds`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getMillisecondsList(path: String): Option[java.util.List[java.lang.Long]] =
    readValue(path, underlying.getDurationList(path, TimeUnit.MILLISECONDS))

  /**
   * Retrieves a configuration value as Seq of `Milliseconds`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getMillisecondsSeq(path: String): Option[Seq[java.lang.Long]] =
    getMillisecondsList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as List of `FiniteDuration`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getFiniteDurationList(path: String): Option[java.util.List[FiniteDuration]] =
    getFiniteDurationSeq(path).map(_.asJava)

  /**
   * Retrieves a configuration value as Seq of `FiniteDuration`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getFiniteDurationSeq(path: String): Option[Seq[FiniteDuration]] =
    readValue(path, underlying.getDurationList(path, TimeUnit.MILLISECONDS))
      .map(asScalaList).map(_.map(FiniteDuration(_,  TimeUnit.MILLISECONDS)))

  /**
   * Retrieves a configuration value as List of `Nanoseconds`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getNanosecondsList(path: String): Option[java.util.List[java.lang.Long]] =
    readValue(path, underlying.getDurationList(path, TimeUnit.NANOSECONDS))

  /**
   * Retrieves a configuration value as Seq of `Nanoseconds`.
   * (ex) engine.timeouts = [1 second, 1 second]
   */
  def getNanosecondsSeq(path: String): Option[Seq[java.lang.Long]] =
    getNanosecondsList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as a List of `Number`.
   * (ex) engine.maxSizes = [50, 500, 5000]
   */
  def getNumberList(path: String): Option[java.util.List[java.lang.Number]] =
    readValue(path, underlying.getNumberList(path))

  /**
   * Retrieves a configuration value as a Seq of `Number`.
   * (ex) engine.maxSizes = [50, 500, 5000]
   */
  def getNumberSeq(path: String): Option[Seq[java.lang.Number]] =
    getNumberList(path).map(asScalaList)

  /**
   * Retrieves a configuration value as a List of `ConfigObject`.
   * (ex) engine.properties = [{id: 5, power: 3}, {id: 6, power: 20}]
   */
  def getObjectList(path: String): Option[java.util.List[_ <: ConfigObject]] =
    readValue[java.util.List[_ <: ConfigObject]](path, underlying.getObjectList(path))

  /**
   * Retrieves a configuration value as a List of `String`.
   * (ex) names = ["Jim", "Bob", "Steve"]
   */
  def getStringList(path: String): Option[java.util.List[java.lang.String]] =
    readValue(path, underlying.getStringList(path))

  /**
   * Retrieves a configuration value as a Seq of `String`.
   * (ex) names = ["Jim", "Bob", "Steve"]
   */
  def getStringSeq(path: String): Option[Seq[java.lang.String]] =
    getStringList(path).map(asScalaList)

  /**
   * Retrieves a ConfigObject for this path, which implements Map<String,ConfigValue>
   * (ex) engine.properties = {id: 1, power: 5}
   */
  def getObject(path: String): Option[ConfigObject] =
    readValue(path, underlying.getObject(path))

  /**
   * Returns available keys.
   */
  def keys: Set[String] =
    underlying.entrySet.asScala.map(_.getKey).toSet

  /**
   * Returns sub-keys.
   */
  def subKeys: Set[String] =
    underlying.root().keySet().asScala.toSet

  /**
   * Returns every path as a set of key to value pairs,
   * by recursively iterating through the config objects.
   */
  def entrySet: Set[(String, ConfigValue)] =
    underlying.entrySet().asScala.map(e => e.getKey -> e.getValue).toSet

  /**
   * Convert to Scala List `Seq`
   */
  private def asScalaList[A](l: java.util.List[A]): Seq[A] =
    asScalaBufferConverter(l).asScala.toList
}
