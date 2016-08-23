/*
 * This file is part of the nextbeat services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import org.specs2.mutable._

object TokenSpec extends Specification {

  "TokenSpec" should {
    "signWithHMAC" in {
      val token  = "securty-token"
      val signed = Token.signWithHMAC(token)
      val opt    = Token.verifyHMAC(signed)
      opt must beSome(token)
    }
  }
}
