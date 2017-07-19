/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

import org.specs2.mutable._

object idSpec extends Specification {

  // モデル
  case class User(
    id:        Option[User.Id],
    name:      String,
    createdAt: java.time.LocalDateTime = NOW,
    updatedAt: java.time.LocalDateTime = NOW
  ) extends EntityModel[User.Id]

  // コンパニオン・オブジェクト
  object User {
    val  Id = the[Identity[Id]]
    type Id = Long @@ User
    def apply(name: String): Entity.WithNoId[Id, User] =
      Entity.WithNoId { User(None, "Kinugasa") }
  }

  // テスト
  "model" should {
    val user1 = User("Kinugasa")
    val user2 = user1.map(_.copy(name = "Sp1rytus"))

    println(user1.v)
    println(user2.v)

    user1.hasId  must_=== false
    user1.v.name must_=== "Kinugasa"
  }
}
