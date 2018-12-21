/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import java.security.SecureRandom
import scala.util.Random

/**
 * The generator to generate a new token as string
 */
case class TokenGenerator(
  protected val  table: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
  protected val random: Random = new Random(new SecureRandom())
) {

  /**
   * Generate a new token as string
   */
  final def next(length: Int): String =
    Iterator.continually(
      random.nextInt(table.size)).map(table).take(length).mkString
}
