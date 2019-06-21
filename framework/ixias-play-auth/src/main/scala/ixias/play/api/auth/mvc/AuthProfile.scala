/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import play.api.{ Environment, Mode }
import play.api.mvc.{ RequestHeader, Result }
import play.api.libs.typedmap.TypedKey

import ixias.model.{ Entity, EntityModel, IdStatus }
import ixias.play.api.auth.token.Token
import ixias.play.api.auth.container.Container
import ixias.play.api.mvc.Errors._

trait AuthProfile[M <: EntityModel, A]
{
  import Token._

  // --[ TypeDefs ]-------------------------------------------------------------
  type Id         = M#Id                              // The identity to detect authenticated resource.
  type Auth       = M                              // The authenticated resource.
  type AuthEntity = Entity[M, IdStatus.Exists]  // The entity which is containing authenticated resource.
  type Authority  = A                              // The type of authoriy

  /** Keys to request attributes. */
  object RequestAttrKey {
    val Auth      = TypedKey[AuthEntity]("Authentication")
    val Authority = TypedKey[Authority]("Authority")
  }

  // --[ Properties ]-----------------------------------------------------------
  /** The enviroment. */
  val env: Environment

  /** The accessor for security token. */
  val tokenAccessor: Token

  /** The datastore for security token. */
  val datastore: Container[Id]

  /** The timeout value in `seconds` */
  val sessionTimeout: Duration = Duration.Inf

  /** Can execute program logic asynchronously */
  implicit val executionContext: scala.concurrent.ExecutionContext

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Resolve authenticated resource by the identity
   */
  def resolve(id: Id): Future[Option[AuthEntity]]

  /**
   * Verifies what authenticated resource authorized to do.
   */
  def authorize(auth: AuthEntity, authority: Option[Authority]): Future[Boolean] =
    Future.successful(true)

  /**
   * Invoked if authentication failed with the credentials provided.
   * This should only be called where an authentication attempt has truly failed
   */
  def authenticationFailed(implicit request: RequestHeader): Result =
    E_AUTHENTICATION

  /**
   * Invoked if authorization failed.
   * Authorization is the process of allowing an authenticated users to
   * access the resources by checking whether the user has access rights to the system.
   * Authorization helps you to control access rights by granting or
   * denying specific permissions to an authenticated user.
   */
  def authorizationFailed(auth: AuthEntity, authority: Option[Authority])(implicit request: RequestHeader): Result =
    E_AUTHRIZATION

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Returns authorized user.
   */
  final def loggedIn(implicit request: RequestHeader): Option[AuthEntity] =
    request.attrs.get(RequestAttrKey.Auth)

  /**
   * Returns the result response.
   */
  final def loggedIn(block: AuthEntity => Result)(implicit request: RequestHeader): Result =
    loggedIn match {
      case Some(auth) => block(auth)
      case None       => authenticationFailed
    }

  /**
   * Returns the result response.
   */
  final def loggedIn(block: AuthEntity => Future[Result])(implicit request: RequestHeader): Future[Result] =
    loggedIn match {
      case Some(auth) => block(auth)
      case None       => Future.successful(authenticationFailed)
    }

  /**
   * Returns the result response of applying block
   * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`
   */
  final def loggedInOrNot(ifEmpty: => Result)(block: AuthEntity => Result)(implicit request: RequestHeader): Result =
    loggedIn match {
      case Some(auth) => block(auth)
      case None       => ifEmpty
    }

  /**
   * Returns the result response of applying block
   * if the user data is nonempty. Otherwise, evaluates expression `ifEmpty`
   */
  final def loggedInOrNot(ifEmpty: => Result)(block: AuthEntity => Future[Result])(implicit request: RequestHeader): Future[Result] =
    loggedIn match {
      case Some(auth) => block(auth)
      case None       => Future.successful(ifEmpty)
    }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Invoke this method on login succeeded.
   */
  final def loginSucceeded(id: Id, block: AuthenticityToken => Result)(implicit request: RequestHeader): Future[Result] =
    (for {
      token  <- datastore.open(id, sessionTimeout)
      result  = block(token)
    } yield tokenAccessor.put(token)(result)) recover {
      case _: Throwable => E_INTERNAL_SERVER
    }

  /**
   * Invoke this method on logout succeeded.
   */
  final def logoutSucceeded(uid: Id, block: => Result)(implicit request: RequestHeader): Future[Result] =
    for {
      _ <- tokenAccessor.extract match {
        case None        => Future.successful(())
        case Some(token) => datastore.destroy(token)
      }
    } yield tokenAccessor.discard(block)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Extract a session token in `RequestHeader`.
   */
  final def extractAuthToken(implicit request: RequestHeader): Option[AuthenticityToken] =
    (env.mode == Mode.Prod match {
      case true  => None
      case false => request.headers.get("TEST_AUTH_TOKEN").map(AuthenticityToken(_))
    }) orElse tokenAccessor.extract

  /**
   * Restore a user data by the session token in `RequestHeader`.
   */
  final def restore(implicit request: RequestHeader): Future[(Option[AuthEntity], Result => Result)] =
    extractAuthToken match {
      case None        => Future.successful(None -> identity)
      case Some(token) => (for {
        Some(id)   <- datastore.read(token)
        Some(auth) <- resolve(id)
        _          <- datastore.setTimeout(token, sessionTimeout)
      } yield {
        Some(auth) -> tokenAccessor.put(token) _
      }) recover {
        case ex: Throwable => None -> identity
      }
    }

  /**
   * Verifies what user are authenticated to do.
   */
  final def authenticate(implicit request: RequestHeader): Future[Either[Result, (AuthEntity, Result => Result)]] =
    restore map {
      case (Some(auth), updater) => Right(auth -> updater)
      case _                     =>  Left(authenticationFailed)
    }

  /**
   * Verifies what user are authorized to do.
   */
  final def authorize(authority: Option[Authority])(implicit request: RequestHeader): Future[Either[Result, (AuthEntity, Result => Result)]] =
    (for {
      Some((auth, updater)) <- authenticate.map(_.toOption)
      authorized            <- authorize(auth, authority)
    } yield authorized match {
      case true  => Right(auth -> updater)
      case false => Left(authorizationFailed(auth, authority))
    }) recover {
      case _: NoSuchElementException => Left(authenticationFailed)
    }
}
