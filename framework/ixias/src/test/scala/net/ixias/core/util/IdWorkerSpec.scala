/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.util

import org.specs2.mutable.Specification

// -----------------------------------------------------------------------------
class IdWorkerSpec extends Specification {
  "IdWorker" should {
    "generate" in {
      val worker = new IdWorker
      val id1    = worker.generate
      val id2    = worker.generate
      id1 > 0 must beTrue
      id2 > 0 must beTrue
      id1 must_!= id2
    }
  }
}
