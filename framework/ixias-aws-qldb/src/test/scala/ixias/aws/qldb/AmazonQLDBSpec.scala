/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import org.specs2.mutable._

import java.time.LocalTime
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import ixias.model._
import ixias.aws.qldb.model.Table

/** Table Data Model */
case class TestData(
  id:   Option[TestData.Id],
  a:    Int,
  b:    String,
  time: Option[LocalTime],
) extends EntityModelWithNoTimeRec[TestData.Id]

object TestData {
  type Id = String @@ TestData
}

/** Table */
object TestTable extends Table {
  val dsn   = DataSourceName("ixias.aws.qldb://ledger/test")
  val query = new Query()
  class Query extends TableQuery("hogehoge") {
    def find            = sql("SELECT id, r.* FROM __TABLE_NAME__ AS r BY id")
    def findByA(a: Int) = sql("SELECT id, r.* FROM __TABLE_NAME__ AS r BY id WHERE r.a = ?", a)
  }
}

/** Repository */
object TestRepository extends AmazonQLDBRepository[TestData.Id, TestData]  {
  import api._
  def findByA(a: String): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(TestTable) { case (db, qldb) =>
      db.execute[TestData](qldb.findByA(1000))
    }
  def get(id: Id): Future[Option[EntityEmbeddedId]] = ???
  def add(data: EntityWithNoId): Future[Id] = ???
  def update(data: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] = ???
  def remove(id: Id): Future[Option[EntityEmbeddedId]] = ???
}


/** Test Specs */
class AmazonQLDBSpec extends Specification {
  "AmazonQLDBSpec" should {
    "Get data from AmazonQLDB" in {
      val f = for {
        dataSeq <- TestRepository.findByA("1000")
      } yield println(dataSeq)
      Await.ready(f, Duration.Inf)
      true must_== true
    }
  }
}
