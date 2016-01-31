/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.security

import java.security.SecureRandom
import scala.util.{ Try, Success, Failure, Random }
import scala.util.control.NonFatal

/** The provider to generate a new token as string */
case class Token(
  protected val  table: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
  protected val random: Random = new Random(new SecureRandom())
) {

  /** Generate a new token as string */
  final def generate(length: Int): String =
    Iterator.continually(
      random.nextInt(table.size)).map(table).take(length).mkString

  /** Do not change this unless you understand the security issues behind timing attacks.
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

/** The component to manage token as string */
trait HasPIN {

  // --[ Properties ]-----------------------------------------------------------
  /** The token provider */
  protected lazy val worker = Token(table = "1234567890")

  /** The generated pin code */
  private var _pin = worker.generate(4)

  // --[ Methods ]--------------------------------------------------------------
  /** Get a PIN code */
  final def pin = _pin

  /** Verify a given PIN code **/
  final def verifyPIN(pin: String): Boolean = worker.safeEquals(_pin, pin)

  /** Override a PIN code by given value */
  final def copyPIN[T <: HasPIN](pin: String): T = {
    _pin = pin
    this.asInstanceOf[T]
  }
}

/** The component to manage pin as numeric number */
trait HasToken {

  // --[ Properties ]-----------------------------------------------------------
  /** The token provider */
  protected lazy val worker = Token(
    table = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  )
  /** The generated token */
  private var _token = worker.generate(32)

  // --[ Methods ]--------------------------------------------------------------
  /** Get a token */
  final def token = _token

  /** Verify a given token **/
  final def verifyToken(token: String): Boolean = worker.safeEquals(_token, token)

  /** Override a token by given value */
  final def copyToken[T <: HasToken](token: String): T = {
    _token = token
    this.asInstanceOf[T]
  }
}
