/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias.core.port.adapter.persistence

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import net.ixias.core.domain.model.{ Identity, Entity }

// -----------------------------------------------------------------------------
case class Test(
  val id:        Option[Test.Id],
  val label:     Int,
  val updatedAt: Option[DateTime],
  val createdAt: Option[DateTime]
) extends Entity[Test.Id]

object Test {
  case class Id(val value: Long) extends Identity[Long]
}

object TestRepository extends BasicRepository[Test.Id, Test] {
}

// -----------------------------------------------------------------------------
class RepositorySpec extends Specification {
  "Repository" should {
    "test" in {
      val exists = true
      exists must_== true
    }
  }
}
