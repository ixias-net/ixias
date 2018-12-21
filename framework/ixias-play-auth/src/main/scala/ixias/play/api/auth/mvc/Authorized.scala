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
 * Provides the custom action for authorization.
 */
trait  AuthorizedActionBuilder extends ActionBuilder[Request, AnyContent]
object AuthorizedActionBuilder {
  def apply[T](auth: AuthProfile[_, _, T], authority: Option[T], parser: BodyParser[AnyContent])
    (implicit ec: ExecutionContext): AuthorizedActionBuilder =
    new AuthorizedActionBuilderImpl(auth, authority, parser)
}

// Implementation for authorization
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class AuthorizedActionBuilderImpl[T](
  val auth:      AuthProfile[_, _, T],
  val authority: Option[T],
  val parser:    BodyParser[AnyContent]
)(implicit val executionContext: ExecutionContext) extends AuthorizedActionBuilder {

  /** Invoke the block. */
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    auth.authorize(authority)(request) flatMap {
      case Left(failed)           => Future.successful(failed)
      case Right((data, updater)) => block {
        request.addAttr(auth.RequestAttrKey.Auth, data)
      } map updater
    }
  }
}
