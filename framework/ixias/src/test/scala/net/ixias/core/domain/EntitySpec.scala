/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.domain.model

import org.joda.time.DateTime
import org.specs2.mutable.Specification

// ユーザ情報
//~~~~~~~~~~~~
case class User(
  val id:        Identity[Long],
  val email:     String,
  val updatedAt: DateTime = new DateTime(),
  val createdAt: DateTime = new DateTime()
) extends Entity[Long]

case class User2Id(val value: Long)
case class User2(
  val id:        Identity[User2Id],
  val email:     String,
  val updatedAt: DateTime = new DateTime(),
  val createdAt: DateTime = new DateTime()
) extends Entity[User2Id]

// テスト
//~~~~~~~~
class EntitySpec extends Specification {
  "Enum" should {
    "user with id as scala variable" in {
      val user = User(SomeId(1), "taro.yamada@nextbeat.net")
      user.id    must_== SomeId(1)
      user.email must_== "taro.yamada@nextbeat.net"
    }
    "user with id as object" in {
      val id   = SomeId(User2Id(1))
      val user = User2(id, "taro.yamada@nextbeat.net")
      user.id    must_== id
      user.email must_== "taro.yamada@nextbeat.net"
    }
    "user with empty id - 1" in {
      val user = User(NoneId, "taro.yamada@nextbeat.net")
      user.id     must_== NoneId
      user.id.get must throwA[NoSuchElementException]
      user.email  must_== "taro.yamada@nextbeat.net"
    }
    "user with empty id - 2" in {
      val user = User2(NoneId, "taro.yamada@nextbeat.net")
      user.id     must_== NoneId
      user.id.get must throwA[NoSuchElementException]
      user.email  must_== "taro.yamada@nextbeat.net"
    }
  }
}
