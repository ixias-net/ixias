/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.service

import _root_.play.api.mvc.{ Result, RequestHeader }
import scala.util.Try
import scala.concurrent.duration.Duration

import play.api.auth.token.Token
import play.api.auth.data.Container
import play.api.auth.mvc.StackRequest
import core.domain.model.{ Identity, Entity }

object Profile {
  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends StackRequest.AttributeKey[Profile#User]
  /** The key of attribute for containing user data. */
  case object AuthorityKey extends StackRequest.AttributeKey[Profile#Authority]
}

trait Profile {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[_]
  /** The type of authority roles */
  type Authority <: Null


  // --[ Properties ]-----------------------------------------------------------
  /** The cookie name */
  val cookieName: String = "sid"

  /** The timeout value in `seconds` */
  val sessionTimeout: Duration = Duration.Inf

  /** The accessor for security token. */
  val tokenAccessor: Token

  /** The datastore for security token. */
  val datastore: Container[Id]

  // --[ Methods ]--------------------------------------------------------------
  /** Resolve user by specified user-id. */
  def resolve(id: Id): Try[Option[User]]

  /** Verifies what user are authorized to do. */
  def authorize(user: User, authority: Option[Authority]): Try[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  /** Invoked if authentication failed with the credentials provided.
    * This should only be called where an authentication attempt has truly failed */
  def authenticationFailed(req: RequestHeader): Result

  /** Invoked if authorization failed.
    * Authorization is the process of allowing an authenticated users to
    * access the resources by checking whether the user has access rights to the system.
    * Authorization helps you to control access rights by granting or
    * denying specific permissions to an authenticated user. */
  def authorizationFailed(req: RequestHeader, user: User, authority: Option[Authority]): Result
}
