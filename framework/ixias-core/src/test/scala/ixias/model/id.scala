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
  case class User[S <: IdStatus](
    _id:  User.Id,
    name: String
  ) extends Entity[User.Id, S]

  // コンパニオン・オブジェクト
  object User {
    type Id = Long @@ User[_]
    val  Id = TagOf[User[_]]
    def apply(name: String):           User[IdStatus.Empty]  = User(Id.empty, name)
    def apply(id: Long, name: String): User[IdStatus.Exists] = User(Id(id),   name)
  }

  "id" should {
    val user1 = User(100, "Kinugasa")
    val user2 = User("EmptyKinugasa")

    println(user1.hasId)
    println(user1.idOpt)
    println(user1.id)

    println(user2.hasId)
    println(user2.idOpt)
    // println(user2.id)
    user1.id must_=== User.Id(100)
  }
}
