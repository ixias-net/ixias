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

import play.api.mvc.{ RequestHeader, Result }
import play.api.libs.iteratee.Execution.Implicits.trampoline

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.digest.DigestUtils
import org.abstractj.kalium.encoders.Encoder
import org.abstractj.kalium.keys.AuthenticationKey
import org.abstractj.kalium.NaCl.Sodium.CRYPTO_AUTH_HMACSHA512256_BYTES

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

  /** The token provider */
  protected lazy val worker = SecurityToken(
    table = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  )

  /** The object that provides some cryptographic operations */
  protected lazy val crypto: AuthenticationKey = {
    val config = ConfigFactory.load()
    val secret = config.getString("session.token.secret")
    new AuthenticationKey(DigestUtils.md5Hex(secret), Encoder.RAW)
  }

  /** Generate a new token as string */
  final def generate(implicit container: Container[_]): Future[AuthenticityToken] = {
    val token = worker.generate(32)
    container.read(token).flatMap {
      case Some(_) => generate
      case None    => Future.successful(token)
    }
  }

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(signedToken: SignedToken): Option[AuthenticityToken] = {
    val (signature, token) = signedToken.splitAt(CRYPTO_AUTH_HMACSHA512256_BYTES * 2)
    crypto.verify(Encoder.RAW.decode(token), Encoder.HEX.decode(signature)) match {
      case true  => Some(token)
      case false => None
    }
  }

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  final def signWithHMAC(token: AuthenticityToken): SignedToken = {
    val signature = crypto.sign(Encoder.RAW.decode(token))
    Encoder.HEX.encode(signature) + token
  }
}
