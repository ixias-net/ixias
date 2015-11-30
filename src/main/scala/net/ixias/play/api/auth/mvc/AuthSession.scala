/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.mvc

import play.api.mvc._
import play.api.mvc.Cookie
import play.api.libs.Crypto
import scala.concurrent.{Future, ExecutionContext}
import net.ixias.play.api.auth.AuthProfile

/** Provides the utility methods for auth session. */
trait AuthSession { self: AuthProfile =>

  /** Invoke this method on login succeeded */
  def gotoLoginSucceeded(userId: Id)
    (implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    gotoLoginSucceeded(userId, loginSucceeded(request))
  }

  def gotoLoginSucceeded(userId: Id, result: => Future[Result])
    (implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    for {
      token  <- datastore.open(userId, sessionTimeout)
      result <- result
    } yield tokenAccessor.put(token)(result)
  }

  /** Invoke this method on logout succeeded */
  def gotoLogoutSucceeded
    (implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    gotoLogoutSucceeded(logoutSucceeded(request))
  }

  def gotoLogoutSucceeded(result: => Future[Result])
    (implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    tokenAccessor.extract(request).foreach(datastore.destroy)
    result.map(tokenAccessor.discard)
  }
}
