/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.model

import org.joda.time.DateTime
import org.specs2.mutable.Specification

// テスト
//~~~~~~~~
class TableSpec extends Specification {
  "Table" should {
    "declare" in {
      val exists = 1
      exists must_== 1
    }
  }
}
