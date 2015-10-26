/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth

import _root_.play.api.Play
import _root_.play.api.mvc.{ RequestHeader, Result }
import scala.concurrent.{ ExecutionContext, Future }

import core.util.EnumOf
import core.domain.model.{ Identity, Entity }
import net.ixias.play.api.auth.token._

trait AuthConfig { config =>

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[Id]
  /** The type of authority roles */
  type Authority <: EnumOf[_]

  // --[ Properties ]-----------------------------------------------------------
  /** The cookie name */
  def cookieName: String
  /** The timeout value in `seconds` */
  def sessionTimeout: Int
  /** The accessor for security token. */
  lazy val token: Token = new CookieToken(
    cookieName           = config.cookieName,
    cookieMaxAge         = Some(config.sessionTimeout),
    cookiePathOption     = "/",
    cookieDomainOption   = None,
    cookieSecureOption   = Play.isProd(Play.current),
    cookieHttpOnlyOption = true
  )

  // --[ Methods ]--------------------------------------------------------------
  def resolve(id: Id)(implicit context: ExecutionContext): Future[Option[User]]
  def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  def       loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def      logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def  authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result]
}
