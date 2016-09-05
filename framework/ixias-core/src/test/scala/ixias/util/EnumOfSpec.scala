/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import org.specs2.mutable._

object EnumOfSpec extends Specification {

  sealed abstract class Color(val red: Double, val green: Double, val blue: Double)
  object Color extends EnumOf[Color] {
    case object Red   extends Color(1, 0, 0)
    case object Green extends Color(0, 1, 0)
    case object Blue  extends Color(0, 0, 1)
    case object White extends Color(0, 0, 0)
    case object Black extends Color(1, 1, 1)
  }
  "EnumOfSpec" should {
    "test_01" in {
      Color.Black.green must_== 1
    }
  }
}
