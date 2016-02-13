/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.model

import org.joda.time.DateTime
import slick.driver.JdbcProfile

import org.specs2.mutable.Specification

// 定義
//~~~~~~
case class UserTableRecord(
  val uid:       String,
  val name:      Option[String],
  val email:     Option[String],
  val updatedAt: DateTime = new DateTime,
  val createdAt: DateTime = new DateTime
)

trait UserTable[P <: JdbcProfile] extends Table[UserTableRecord, P] {
}

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
