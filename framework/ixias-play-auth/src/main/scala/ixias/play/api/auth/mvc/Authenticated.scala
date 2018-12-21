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
trait  AuthenticatedActionBuilder extends ActionBuilder[Request, AnyContent]
object AuthenticatedActionBuilder {
  def apply(auth: AuthProfile[_, _, _], parser: BodyParser[AnyContent])
    (implicit ec: ExecutionContext): AuthenticatedActionBuilder =
    new AuthenticatedActionBuilderImpl(auth, parser)
}

// Implementation for authentication
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class AuthenticatedActionBuilderImpl(
  val auth:   AuthProfile[_, _, _],
  val parser: BodyParser[AnyContent]
)(implicit val executionContext: ExecutionContext) extends AuthenticatedActionBuilder {

  /** Invoke the block. */
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    auth.authenticate(request) flatMap {
      case Left(failed)           => Future.successful(failed)
      case Right((data, updater)) => block {
        request.addAttr(auth.RequestAttrKey.Auth, data)
      } map updater
    }
  }
}
