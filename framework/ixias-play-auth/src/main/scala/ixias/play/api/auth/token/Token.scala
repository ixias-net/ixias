/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import scala.concurrent.Future
import play.api.mvc.{RequestHeader, Result}
import play.api.libs.iteratee.Execution.Implicits.trampoline
import com.typesafe.config.ConfigFactory

import ixias.security.{ TokenSigner, RandomStringToken }
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

  /** The object that provides some cryptographic operations */
  protected lazy val signer = TokenSigner()

  /** Generate a new token as string */
  final def generate(implicit container: Container[_]): Future[AuthenticityToken] = {
    val token = RandomStringToken.next(32)
    container.read(token).flatMap {
      case Some(_) => generate
      case None    => Future.successful(token)
    }
  }

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(signedToken: SignedToken): Option[AuthenticityToken] =
    signer.verify(signedToken).toOption

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  final def signWithHMAC(token: AuthenticityToken): SignedToken =
    signer.sign(token)
}
