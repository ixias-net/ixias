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
case class UserId(val value: Long) extends Identity[Long]
case class User(
  val id:        Option[UserId],
  val email:     String,
  val updatedAt: DateTime = new DateTime(),
  val createdAt: DateTime = new DateTime()
) extends Entity[UserId]

// テスト
//~~~~~~~~
class EntitySpec extends Specification {
  "Enum" should {
    "indexOf" in {
      val exists = 1
      val user = User(Some(UserId(1)), "taro.yamada@nextbeat.net")
      println(user.updatedAt)
      println(user.createdAt)

      SomeId(1)
      NoneId(1)

      exists must_== 1
    }
  }
}
