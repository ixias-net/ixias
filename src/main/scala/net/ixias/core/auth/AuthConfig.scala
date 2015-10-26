/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play2.auth

import play.api.mvc.{ RequestHeader, Result }
import scala.concurrent.{ ExecutionContext, Future }

import core.util.EnumOf
import core.domain.model.{ Identity, Entity }

trait AuthConfig {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of user identity */
  type Id   <: Identity[_]
  /** The type of user entity */
  type User <: Entity[Id]
  /** The type of authority roles */
  type Authority <: EnumOf[_]

  // --[ Properties ]-----------------------------------------------------------
  /** Specified the timeout value in `seconds` */
  def sessionTimeout: Int

  // --[ Methods ]--------------------------------------------------------------
  def resolve(id: Id)(implicit context: ExecutionContext): Future[Option[User]]
  def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean]

  // --[ Methods ]--------------------------------------------------------------
  def       loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def      logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]
  def  authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result]
}
