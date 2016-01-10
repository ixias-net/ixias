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

// テスト
//~~~~~~~~
class EntitySpec extends Specification {
  "Enum" should {
    "indexOf" in {
      val user = User(SomeId(1), "taro.yamada@nextbeat.net")
      user.id     must_== SomeId(1)
      user.id.get must_== 1
      user.email  must_== "taro.yamada@nextbeat.net"
    }
  }
}
