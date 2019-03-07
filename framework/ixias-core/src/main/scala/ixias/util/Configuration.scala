/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 *
 * -----------------------------------------------------------------------------
 *
 * Note:
 *  Reference of the source code from the following point of view.
 *  ・Depending on the function of ConfigLoader.
 *    Behavior of easily obtaining the value of specified type via type function of get function
 *
 *  https://github.com/playframework/playframework/blob/2.5.x/framework/src/play/src/main/scala/play/api/Configuration.scala#L1030
 *
 * Copyright (C) 2009-2019 Lightbend Inc. (https://www.lightbend.com).
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this project except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package ixias.util

import com.typesafe.config._
import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, FiniteDuration, _ }

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
   * Get the config at the given path.
   */
  def get[A](path: String)(implicit loader: ConfigLoader[A]): A = {
    loader.load(underlying, path)
  }

  /**
   * Check if the given path exists.
   */
  def has(path: String): Boolean = underlying.hasPath(path)

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
   * Reads a value from the underlying implementation.
   * If the value is not set this will return None, otherwise returns Some.
   */
  private def readValue[T](path: String, v: => T): Option[T] =
    if (underlying.hasPathOrNull(path)) Some(v) else None
}

/**
 * A config loader
 */
trait ConfigLoader[A] { self =>
  def load(config: Config, path: String = ""): A
  def map[B](f: A => B): ConfigLoader[B] = new ConfigLoader[B] {
    def load(config: Config, path: String): B = {
      f(self.load(config, path))
    }
  }
}

object ConfigLoader {

  def apply[A](f: Config => String => A): ConfigLoader[A] = new ConfigLoader[A] {
    def load(config: Config, path: String): A = f(config)(path)
  }

  import scala.collection.JavaConverters._

  // Retrieves a configuration value as an specified type
  implicit val booleanLoader:  ConfigLoader[Boolean]          = ConfigLoader(_.getBoolean)
  implicit val intLoader:      ConfigLoader[Int]              = ConfigLoader(_.getInt)
  implicit val longLoader:     ConfigLoader[Long]             = ConfigLoader(_.getLong)
  implicit val numberLoader:   ConfigLoader[Number]           = ConfigLoader(_.getNumber)
  implicit val doubleLoader:   ConfigLoader[Double]           = ConfigLoader(_.getDouble)
  implicit val stringLoader:   ConfigLoader[String]           = ConfigLoader(_.getString)
  implicit val bytesLoader:    ConfigLoader[ConfigMemorySize] = ConfigLoader(_.getMemorySize)
  implicit val finiteLoader:   ConfigLoader[FiniteDuration]   = ConfigLoader(_.getDuration).map(_.toNanos.nanos)
  implicit val durationLoader: ConfigLoader[Duration]         = ConfigLoader(
    config => path => (config.getIsNull(path)) match {
      case true  => Duration.Inf
      case false => config.getDuration(path).toNanos.nanos
    })

  // Retrieves a configuration value as a Seq of sepecified type.
  implicit val seqBooleanLoader:  ConfigLoader[Seq[Boolean]]          = ConfigLoader(_.getBooleanList).map(_.asScala.map(_.booleanValue))
  implicit val seqIntLoader:      ConfigLoader[Seq[Int]]              = ConfigLoader(_.getIntList).map(_.asScala.map(_.toInt))
  implicit val seqLongLoader:     ConfigLoader[Seq[Long]]             = ConfigLoader(_.getDoubleList).map(_.asScala.map(_.longValue))
  implicit val seqNumberLoader:   ConfigLoader[Seq[Number]]           = ConfigLoader(_.getNumberList).map(_.asScala)
  implicit val seqDoubleLoader:   ConfigLoader[Seq[Double]]           = ConfigLoader(_.getDoubleList).map(_.asScala.map(_.doubleValue))
  implicit val seqStringLoader:   ConfigLoader[Seq[String]]           = ConfigLoader(_.getStringList).map(_.asScala)
  implicit val seqBytesLoader:    ConfigLoader[Seq[ConfigMemorySize]] = ConfigLoader(_.getMemorySizeList).map(_.asScala)
  implicit val seqFiniteLoader:   ConfigLoader[Seq[FiniteDuration]]   = ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos))
  implicit val seqDurationLoader: ConfigLoader[Seq[Duration]]         = ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos))

  // For Configuratin loader.
  implicit val configLoader:           ConfigLoader[Config]             = ConfigLoader(_.getConfig)
  implicit val configObjectLoader:     ConfigLoader[ConfigObject]       = ConfigLoader(_.getObject)
  implicit val configListLoader:       ConfigLoader[ConfigList]         = ConfigLoader(_.getList)
  implicit val seqConfigLoader:        ConfigLoader[Seq[Config]]        = ConfigLoader(_.getConfigList).map(_.asScala)
  implicit val configurationLoader:    ConfigLoader[Configuration]      = configLoader.map(Configuration(_))
  implicit val seqConfigurationLoader: ConfigLoader[Seq[Configuration]] = seqConfigLoader.map(_.map(Configuration(_)))

  /**
   * Loads a value, interpreting a null value as None and any other value as Some(value).
   */
  implicit def optionLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Option[A]] = new ConfigLoader[Option[A]] {
    def load(config: Config, path: String): Option[A] =
      (config.hasPath(path) && !config.getIsNull(path)) match {
        case false => None
        case true  => Some(valueLoader.load(config, path))
      }
  }
  implicit def mapLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Map[String, A]] = new ConfigLoader[Map[String, A]] {
    def load(config: Config, path: String): Map[String, A] = {
      val obj  = config.getObject(path)
      val conf = obj.toConfig
      obj.keySet().asScala.map { key =>
        key -> valueLoader.load(conf, key)
      }.toMap
    }
  }
}
