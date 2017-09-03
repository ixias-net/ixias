/*
 * This file is part of the nextbeat services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

import org.specs2.mutable._
import org.joda.time.DateTime
import slick.jdbc.JdbcProfile

// ユーザ情報
//~~~~~~~~~~~~
case class User(
  val id:        Option[Long],              // ユーザID
  val email:     Option[String],            // E-Mail
  val updatedAt: DateTime = new DateTime(), // データ更新日
  val createdAt: DateTime = new DateTime()  // データ作成日
)

case class UserTable[P <: JdbcProfile](val driver: P)
    extends Table[User, P] { self =>
  import api._

  lazy val query = new Query
  lazy val dsn   = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/sitter"),
    "slave"  -> DataSourceName("ixias.db.mysql://slave/sitter")
  )

  class Query extends BasicQuery(new Table(_)) {
    def test01(id: Option[Long]) = this.filter(_.uid === 0L)
    def test02(datetime: Option[DateTime]) = this.filter(_.createdAt < datetime)
  }
  class Table(tag: Tag) extends BasicTable(tag, "us er") {
    def uid       = column[Option[Long]]   ("uid",        O.UInt64, O.PrimaryKey)
    def email     = column[Option[String]] ("email",      O.UInt64)
    def updatedAt = column[DateTime]       ("updated_at", O.TsCurrent)
    def createdAt = column[DateTime]       ("created_at", O.Ts)
    def * = (uid, email, updatedAt, createdAt) <> (User.tupled, User.unapply)
  }
}

// テスト定義
//~~~~~~~~~~~~
object ExecutionSpec extends Specification {
  "ExecutionSpec" should {
    "model" in {
      val exists = true

      exists must_== true
    }
  }
}
