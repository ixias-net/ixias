/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import scala.util.Random
import scala.concurrent.Future
import java.security.SecureRandom

import play.api.libs.Crypto
import play.api.mvc.{ RequestHeader, Result }

import ixias.security.{ Token => SecurityToken }
import ixias.play.api.auth.container.Container

// The security token
//~~~~~~~~~~~~~~~~~~~~
trait Token {

  /** Extract a security token from storage */
  def extract(request: RequestHeader): Option[AuthenticityToken]

  /** Put a specified security token to storage */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result

  /** Discard a security token in storage */
  def discard(result: Result)(implicit request: RequestHeader): Result

}

// Companion object
//~~~~~~~~~~~~~~~~~~
object Token {
  import scala.concurrent.ExecutionContext.Implicits.global

  /** The token provider */
  protected lazy val worker = SecurityToken(
    table = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  )

  /** Generate a new token as string */
  final def generate(implicit container: Container[_]): Future[AuthenticityToken] = {
    val token = worker.generate(32)
    container.read(token).flatMap {
      case Some(_) => generate
      case None    => Future.successful(token)
    }
  }

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(token: SignedToken): Option[AuthenticityToken] = {
    val (hmac, value) = token.splitAt(40)
    worker.safeEquals(Crypto.sign(value), hmac) match {
      case true  => Some(value)
      case false => None
    }
  }

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  final def signWithHMAC(token: AuthenticityToken): SignedToken =
    Crypto.sign(token) + token

}
