/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import java.security.SecureRandom
import scala.util.{ Try, Success, Failure, Random }
import scala.util.control.NonFatal

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
