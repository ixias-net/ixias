/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import org.specs2.mutable._

object idSpec extends Specification {
  import id._

  // モデル
  case class User(
    _id:  User.Id,
    name: String
  ) extends Entity[User.Id]

  // コンパニオン・オブジェクト
  object User {
    val  Id = id.of[Long, User]
    type Id = id.IdType[Long, User]
    def apply(id: Long, name: String): User = User(Id(id), name)
  }

  "id" should {
    val user1 = User(100,           "Kinugasa")
    val user2 = User(User.Id.empty, "Kinugasa No Id")


    user1._id must_=== 100
  }
}
