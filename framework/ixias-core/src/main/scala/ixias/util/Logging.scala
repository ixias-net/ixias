/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.reflect.ClassTag
import org.slf4j.{ LoggerFactory, Logger => Slf4jLogger }

/**
 * A Logger object is used to log messages for a specific system or application component.
 */
trait Logging {
  protected[this] lazy val logger = {
    val n   = getClass.getName
    val cln = if (n endsWith "$") n.substring(0, n.length - 1) else n
    new Logger(LoggerFactory.getLogger(cln))
  }
}

/**
 * The Logger is a convenient and performant logging library wrapping SLF4J.
 */
sealed class Logger(val slf4jLogger: Slf4jLogger) {

  @inline def isDebugEnabled = slf4jLogger.isDebugEnabled()

  // --[ Methods ]-----------------------------------------------------------
  @inline def error(msg: => String) { if (slf4jLogger.isErrorEnabled) slf4jLogger.error(msg) }
  @inline def  warn(msg: => String) { if (slf4jLogger.isWarnEnabled)  slf4jLogger.warn(msg)  }
  @inline def  info(msg: => String) { if (slf4jLogger.isInfoEnabled)  slf4jLogger.info(msg)  }
  @inline def debug(msg: => String) { if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(msg) }
  @inline def trace(msg: => String) { if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(msg) }

  // --[ Methods ]-----------------------------------------------------------
  @inline def error(msg: => String, t: Throwable) { if (slf4jLogger.isErrorEnabled) slf4jLogger.error(msg, t) }
  @inline def  warn(msg: => String, t: Throwable) { if (slf4jLogger.isWarnEnabled)  slf4jLogger.warn(msg, t)  }
  @inline def  info(msg: => String, t: Throwable) { if (slf4jLogger.isInfoEnabled)  slf4jLogger.info(msg, t)  }
  @inline def debug(msg: => String, t: Throwable) { if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(msg, t) }
  @inline def trace(msg: => String, t: Throwable) { if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(msg, t) }
}

/**
 * The logger's companion object
 */
object Logger {
  def apply[T](implicit ct: ClassTag[T]): Logger =
    new Logger(LoggerFactory.getLogger(ct.runtimeClass))
}
