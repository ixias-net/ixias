/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.mvc

import play.api.mvc.Result
import scala.concurrent.Future
import net.ixias.play.api.mvc.StackAction
import net.ixias.play.api.auth.AuthProfile

/** Provides the utility methods for authorization. */
trait AuthorizeAction extends Action with StackAction with Authorization {
  self: AuthProfile =>

  // --[ Properties ]-----------------------------------------------------------
  /** Import class types for StackRequest attributes. */
  import StackRequest._

  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends AttributeKey[User]
  /** The key of attribute for containing user data. */
  case object AuthorityKey extends AttributeKey[Authority]

  // --[ Methods ]--------------------------------------------------------------
  override def proceed[A](req: StackRequest[A])(f: StackRequest[A] => Future[Result]): Future[Result] = {
    implicit val (request, ctx) = (req, createStackActionExecutionContext(req))
    req.get(AuthorityKey).map(
      authority => authorized(authority) flatMap {
        case Left(result)           => Future.successful(result)
        case Right((user, updater)) => super.proceed(req.set(UserKey, user))(f).map(updater)
      }
    ).getOrElse(
      restoreUser collect {
        case (Some(user), _) => user
      } flatMap {
        authorizationFailed(req, _, None)
      } recoverWith {
        case _ => authenticationFailed(req)
      }
    )
  }
}

