/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core
package util

import com.typesafe.config._
import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

object ConfigExt {
  @inline implicit def extMethodOps(a: Config) = new ExtMethodOps(a)

  class ExtMethodOps(val self: Config) extends AnyVal {
    import scala.collection.JavaConverters._

    // ---------------------------------------------------------------------------
    def         getIntOpt(path: String) = getValueOpt(path)(_.getInt(path))
    def     getBooleanOpt(path: String) = getValueOpt(path)(_.getBoolean(path))
    def      getStringOpt(path: String) = getValueOpt(path)(_.getString(path))
    def  getPropertiesOpt(path: String) = getValueOpt(path)(self => new ExtMethodOps(self.getConfig(path)).toProperties)

    // ---------------------------------------------------------------------------
    def          getIntOr(path: String, default: => Int        = 0)                     = getValueOr(path, default)(_.getInt(path))
    def       getStringOr(path: String, default: => String     = null)                  = getValueOr(path, default)(_.getString(path))
    def       getConfigOr(path: String, default: => Config     = ConfigFactory.empty()) = getValueOr(path, default)(_.getConfig(path))
    def      getBooleanOr(path: String, default: => Boolean    = false)                 = getValueOr(path, default)(_.getBoolean(path))
    def     getDurationOr(path: String, default: => Duration   = Duration.Zero)         = getValueOr(path, default)(self => Duration(self.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS))
    def   getPropertiesOr(path: String, default: => Properties = null)                  = getValueOr(path, default)(self => new ExtMethodOps(self.getConfig(path)).toProperties)
    def getMillisecondsOr(path: String, default: => Long       = 0L)                    = getValueOr(path, default)(_.getDuration(path, TimeUnit.MILLISECONDS))

    // ---------------------------------------------------------------------------
    protected def getValueOpt[T](path: String)(f: Config => T): Option[T]     = if(self.hasPath(path)) Some(f(self)) else None
    protected def  getValueOr[T](path: String, default: T)(f: Config => T): T = if(self.hasPath(path))      f(self)  else default

    def toProperties: Properties = {
      def toProps(m: mutable.Map[String, ConfigValue]): Properties = {
        val props = new Properties(null)
        m.foreach { case (k, cv) =>
          val v =
            if      (cv.valueType() == ConfigValueType.OBJECT) toProps(cv.asInstanceOf[ConfigObject].asScala)
            else if (cv.unwrapped eq null) null
            else     cv.unwrapped.toString
          if(v ne null) props.put(k, v)
        }
        props
      }
      toProps(self.root.asScala)
    }
  }
}

