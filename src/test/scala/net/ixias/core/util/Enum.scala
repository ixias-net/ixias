/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.util

import org.specs2.mutable.Specification

// -----------------------------------------------------------------------------
sealed abstract class Test(
  val code:  Int,
  val label: String
)

object Test extends EnumOf[Test] {
  case object LABEL_1 extends Test(code = 1, label = "LABEL-1")
  case object LABEL_2 extends Test(code = 2, label = "LABEL-2")
}

// -----------------------------------------------------------------------------
class EnumSpec extends Specification {
  "Enum" should {
    "indexOf" in {
      Test.indexOf(Test.LABEL_1) must_== 0
      Test.indexOf(Test.LABEL_2) must_== 1
    }
    "withName" in {
      Test.withName("LABEL_1") must_== Test.LABEL_1
      Test.withName("LABEL_2") must_== Test.LABEL_2
      Test.withName("LABEL_")  must throwA[NoSuchElementException]
      Test.withName("label_1") must throwA[NoSuchElementException]
      Test.withName("label_2") must throwA[NoSuchElementException]
    }
    "withNameOption" in {
      Test.withNameOption("LABEL_1") must_== Some(Test.LABEL_1)
      Test.withNameOption("LABEL_2") must_== Some(Test.LABEL_2)
      Test.withNameOption("LABEL_")  must_== None
      Test.withNameOption("label_1") must_== None
      Test.withNameOption("label_2") must_== None
    }
    "withNameInsensitive" in {
      Test.withNameInsensitive("label_1") must_== Test.LABEL_1
      Test.withNameInsensitive("label_2") must_== Test.LABEL_2
      Test.withNameInsensitive("Label_1") must_== Test.LABEL_1
      Test.withNameInsensitive("Label_2") must_== Test.LABEL_2
      Test.withNameInsensitive("Label_")  must throwA[NoSuchElementException]
    }
    "withNameInsensitiveOption" in {
      Test.withNameInsensitiveOption("label_1") must_== Some(Test.LABEL_1)
      Test.withNameInsensitiveOption("label_2") must_== Some(Test.LABEL_2)
      Test.withNameInsensitiveOption("Label_1") must_== Some(Test.LABEL_1)
      Test.withNameInsensitiveOption("Label_2") must_== Some(Test.LABEL_2)
      Test.withNameInsensitiveOption("Label_")  must_== None
    }
  }
}
