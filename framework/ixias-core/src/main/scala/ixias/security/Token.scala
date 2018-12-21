/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

/**
 * The component to provides common methods.
 */
trait Token {

  protected val generator: TokenGenerator

  /**
   * Generate a new token as string
   */
  def next(length: Int): String = generator.next(length)

  /**
   * Do not change this unless you understand the security issues behind timing attacks.
   * This method intentionally runs in constant time if the two strings have the same length.
   */
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

/**
 * The component to manage token as string
 */
object RandomPINCode extends Token {

  /** The token provider */
  protected val generator = TokenGenerator(
    table = "1234567890"
  )
}

/**
 * The component to manage pin as numeric number
 */
object RandomStringToken extends Token {

  /** The token provider */
  protected val generator = TokenGenerator(
    table = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  )
}
