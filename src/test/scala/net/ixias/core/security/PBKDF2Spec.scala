/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.security

import org.specs2.mutable.Specification

class PBKDF2Spec extends Specification {
  "PBKDF2" should {
    "hash" in {
      val hash = PBKDF2.hash("hogehoge")
      hash must have length(be_>(0))
      PBKDF2.extractSalt(hash).get must have length(32)
      PBKDF2.extractSaltLength(hash).get must_== 32
      PBKDF2.extractIterations(hash).get must beBetween(PBKDF2.HASH_ITERATIONS_MIN, PBKDF2.HASH_ITERATIONS_MAX)
    }
    "compare" in {
      val hash = PBKDF2.hash("hogehoge")
      PBKDF2.compare("hogehoge", hash) must_== true
      PBKDF2.compare("hoeghoge", hash) must_== false
    }
  }
}
