/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.play.api.auth.token

import play.api.libs.Crypto
import play.api.mvc.{ RequestHeader, Result }
import scala.util.Random
import scala.annotation.tailrec
import java.security.SecureRandom

import net.ixias.play.api.auth.datastore.Container

trait Token {

  /** Put a specified security token to storage */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result

  /** Extract a security token from storage */
  def extract(request: RequestHeader): Option[AuthenticityToken]

  /** Discard a security token in storage */
  def discard(result: Result)(implicit request: RequestHeader): Result

  /** Verifies a given HMAC on a piece of data */
  protected def verifyHMAC(token: SignedToken): Option[AuthenticityToken] = {
    val (hmac, value) = token.splitAt(40)
    if (safeEquals(Crypto.sign(value), hmac)) Some(value) else None
  }

  /** Signs the given String with HMAC-SHA1 using the secret token.*/
  protected def signWithHMAC(token: AuthenticityToken): SignedToken =
    Crypto.sign(token) + token

  /* Do not change this unless you understand the security issues behind timing attacks.
   * This method intentionally runs in constant time if the two strings have the same length. */
  protected def safeEquals(a: String, b: String) = {
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

// Companion object
//~~~~~~~~~~~~~~~~~~
object Token {

  val table  = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
  val random = new Random(new SecureRandom())

  /** Generate a new token as string */
  @tailrec final def generate(implicit container: Container[_]): AuthenticityToken = {
    val token  = Iterator.continually(random.nextInt(table.size)).map(table).take(64).mkString
    if (container.read(token).isDefined) generate else token
  }
}
