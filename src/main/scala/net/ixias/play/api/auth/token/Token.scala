/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.token

import _root_.play.api.libs.Crypto
import _root_.play.api.mvc.{ RequestHeader, Result }
import scala.util.{ Try, Random }
import java.security.SecureRandom
import net.ixias.play.api.auth.data.Container

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

  protected val table  = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  protected val random = new Random(new SecureRandom())

  /** Generate a new token as string */
  final def generate(implicit container: Container[_]): Try[AuthenticityToken] = {
    val token = Iterator.continually(random.nextInt(table.size)).map(table).take(32).mkString
    container.read(token) flatMap {
      case Some(_) => generate
      case None    => Try(token)
    }
  }

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  final def signWithHMAC(token: AuthenticityToken): SignedToken =
    Crypto.sign(token) + token

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(token: SignedToken): Option[AuthenticityToken] = {
    val (hmac, value) = token.splitAt(40)
    if (safeEquals(Crypto.sign(value), hmac)) Some(value) else None
  }

  /* Do not change this unless you understand the security issues behind timing attacks.
   * This method intentionally runs in constant time if the two strings have the same length. */
  final def safeEquals(a: String, b: String) = {
    if (a.length != b.length) {
      false
    } else {
      var equal = 0
      for (i <- Array.range(0, a.length)) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }
}
