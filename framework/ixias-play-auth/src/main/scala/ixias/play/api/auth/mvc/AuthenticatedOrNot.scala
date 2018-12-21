/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import play.api.mvc._
import scala.concurrent.{ Future, ExecutionContext }

/**
 * Provides the custom action for authentication.
 */
trait  AuthenticatedOrNotActionBuilder extends ActionBuilder[Request, AnyContent]
object AuthenticatedOrNotActionBuilder {
  def apply(auth: AuthProfile[_, _, _], parser: BodyParser[AnyContent])
    (implicit ec: ExecutionContext): AuthenticatedOrNotActionBuilder =
    new AuthenticatedOrNotActionBuilderImpl(auth, parser)
}

// Implementation for authentication
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class AuthenticatedOrNotActionBuilderImpl(
  val auth:   AuthProfile[_, _, _],
  val parser: BodyParser[AnyContent]
)(implicit val executionContext: ExecutionContext) extends AuthenticatedOrNotActionBuilder {

  /** Invoke the block. */
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    auth.restore(request) flatMap {
      case (None,       updater) => block(request).map(updater)
      case (Some(data), updater) => block {
        request.addAttr(auth.RequestAttrKey.Auth, data)
      } map updater
    }
  }
}
