/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import play.api.mvc._
import play.api.libs.typedmap.TypedKey
import org.uaparser.scala.Parser
import scala.concurrent.{ Future, ExecutionContext }

/** The request attributes. */
object DeviceDetectionAttrKey {
  val OS        = TypedKey[org.uaparser.scala.OS]("OS")
  val Device    = TypedKey[org.uaparser.scala.Device]("Device")
  val UserAgent = TypedKey[org.uaparser.scala.UserAgent]("UserAgent")
  val IsMobile  = TypedKey[Boolean]("IsMobile")
}

/**
 * Provides the custom action to detect device by User-Agent
 */
trait  DeviceDetectionBuilder extends ActionBuilder[Request, AnyContent]
object DeviceDetectionBuilder {
  def apply(parser: BodyParser[AnyContent])(implicit ec: ExecutionContext): DeviceDetectionBuilder =
    new DeviceDetectionBuilderImpl(parser)
}

// Implementation for device detection.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class DeviceDetectionBuilderImpl(
  val parser: BodyParser[AnyContent]
)(implicit val executionContext: ExecutionContext) extends DeviceDetectionBuilder {

  val MOBILE_UA_REGEX = "(iPhone|webOS|iPod|Android|BlackBerry|mobile|SAMSUNG|IEMobile|OperaMobi)".r.unanchored

  /** Invoke the block. */
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) =
    request.headers.get("User-Agent") match {
      case None     => block(request)
      case Some(ua) => block {
        val client = Parser.get.parse(ua)
        request
          .addAttr(DeviceDetectionAttrKey.OS,        client.os)
          .addAttr(DeviceDetectionAttrKey.Device,    client.device)
          .addAttr(DeviceDetectionAttrKey.UserAgent, client.userAgent)
          .addAttr(DeviceDetectionAttrKey.IsMobile,  ua match {
            case MOBILE_UA_REGEX(_) => true
            case _                  => false
          })
      }
    }
}
