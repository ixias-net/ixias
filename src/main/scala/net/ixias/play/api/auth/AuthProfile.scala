/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth

import play.api.Play
import play.api.mvc.{ RequestHeader, Result }
import scala.concurrent.{ ExecutionContext, Future }

import net.ixias.play.api.auth.token._
import net.ixias.play.api.auth.datastore._
import net.ixias.core.domain.model.{ Identity, Entity }

trait AuthProfile { self =>

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[Id]
  /** The type of authority roles */
  type Authority

  // --[ Properties ]-----------------------------------------------------------
  /** The cookie name */
  def cookieName: String
  /** The timeout value in `seconds` */
  def sessionTimeout: Int

  /** The accessor for security token. */
  lazy val tokenAccessor: Token = new CookieToken(
    cookieName           = self.cookieName,
    cookieMaxAge         = Some(self.sessionTimeout),
    cookiePathOption     = "/",
    cookieDomainOption   = None,
    cookieSecureOption   = Play.isProd(Play.current),
    cookieHttpOnlyOption = true
  )

  /** The datastore for security token. */
  lazy val datastore: WrappedContainer[Id] = WrappedContainer(CacheContainer[Id])

  // --[ Methods ]--------------------------------------------------------------
  /** Resolve user by specified user-id. */
  def resolve(id: Id)(implicit context: ExecutionContext): Future[Option[User]]

  /** Ask the authenticator that has been registered with the system for a password. */
  def authenticate(id: Id, password: String)(implicit context: ExecutionContext): Option[User]

  /** Verifies what user are authorized to do. */
  def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  /** Invoked after a user has successfully logged in. */
  def loginSucceeded(request: RequestHeader)
    (implicit context: ExecutionContext): Future[Result]

  /** Invoked after a user has successfully logged out. */
  def logoutSucceeded(request: RequestHeader)
    (implicit context: ExecutionContext): Future[Result]

  /** Invoked if authentication failed with the credentials provided.
    * This should only be called where an authentication attempt has truly failed */
  def authenticationFailed(request: RequestHeader)
    (implicit context: ExecutionContext): Future[Result]

  /** Invoked if authorization failed.
    * Authorization is the process of allowing an authenticated users to
    * access the resources by checking whether the user has access rights to the system.
    * Authorization helps you to control access rights by granting or
    * denying specific permissions to an authenticated user. */
  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])
    (implicit context: ExecutionContext): Future[Result]
}
