/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import _root_.play.api.mvc._
import scala.concurrent.Future
import scala.util.{ Right, Left }

import play.api.auth.AuthProfile
import play.api.auth.mvc.StackRequest._

/** Provides the utility methods for authentication. */
class AuthenticatedBuilder(params: Attribute[_]*)(implicit auth: AuthProfile) extends StackActionBuilder {

  /** Proceed with the next advice or target method invocation */
  override def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = {
    implicit val ctx = createStackActionExecutionContext(req)
    auth.authenticate(req) match {
      case Left(result) => Future.successful(result)
      case Right((user, updater)) => super.proceed(req.set(AuthProfile.UserKey, user))(f).map(updater)
    }
  }
}

/** The custom playframework action. */
object Authenticated {

  /** Constructs an `Action` with default content, and no request parameter. */
  final def apply(f: StackRequest[AnyContent] => Result)
    (implicit auth: AuthProfile): Action[AnyContent] = new AuthenticatedBuilder().apply(f)

  /** Constructs an `Action` with default content. */
  final def apply(params: Attribute[_]*)(f: StackRequest[AnyContent] => Result)
    (implicit auth: AuthProfile): Action[AnyContent] = new AuthenticatedBuilder(params: _*).apply(f)

  /** Constructs an `Action` with default content. */
  final def apply[A](p: BodyParser[A], params: Attribute[_]*)(f: StackRequest[A] => Result)
    (implicit auth: AuthProfile): Action[A] = new AuthenticatedBuilder(params: _*).apply(p)(f)
}
