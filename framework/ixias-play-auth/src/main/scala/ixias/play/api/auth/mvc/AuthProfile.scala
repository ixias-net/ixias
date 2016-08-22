/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.Duration

import play.api.Environment
import play.api.mvc.{ RequestHeader, Result, Results }
import play.api.libs.iteratee.Execution

import ixias.model.{ Identity, Entity }
import ixias.play.api.auth.token.Token
import ixias.play.api.auth.container.Container

trait AuthProfile extends AuthProfileLike with Results
{
  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[_]
  /** The type of authority roles */
  type Authority >: Null

  /** The key of attribute for containing required authority roles. */
  case object UserKey      extends ActionRequest.AttributeKey[User]
  /** The key of attribute for containing user data. */
  case object AuthorityKey extends ActionRequest.AttributeKey[Authority]

  // --[ Properties ]-----------------------------------------------------------
  /** The environment for the play application. */
  implicit val env: Environment

  /** Can execute program logic asynchronously */
  implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  /** The timeout value in `seconds` */
  val sessionTimeout: Duration = Duration.Inf

  /** The accessor for security token. */
  val tokenAccessor: Token

  /** The datastore for security token. */
  val datastore: Container[Id]

  // --[ Methods ]--------------------------------------------------------------
  /** Resolve user by specified user-id. */
  def resolve(id: Id): Future[Option[User]]

  /** Verifies what user are authorized to do. */
  def authorize(user: User, authority: Option[Authority]): Future[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  /** Invoke this method on login succeeded */
  def loginSucceeded(id: Id)(implicit req: RequestHeader): Future[Result]

  /** Invoke this method on logout succeeded */
  def logoutSucceeded(id: Id)(implicit req: RequestHeader): Future[Result]

  /** Invoked if authentication failed with the credentials provided.
    * This should only be called where an authentication attempt has truly failed */
  def authenticationFailed(implicit req: RequestHeader): Result

  /** Invoked if authorization failed.
    * Authorization is the process of allowing an authenticated users to
    * access the resources by checking whether the user has access rights to the system.
    * Authorization helps you to control access rights by granting or
    * denying specific permissions to an authenticated user. */
  def authorizationFailed(user: User, authority: Option[Authority])(implicit req: RequestHeader): Result
}
