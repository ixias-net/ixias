package ixias.persistence.backend

import ixias.persistence.model.DataSourceName
import org.specs2.mutable._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class BasicDatabaseContainerTest extends Specification {

    case class Database() {
        var count = 0
        def incrementAndGet(): Database = {
            count += 1
            this
        }
    }

    val container = new BasicDatabaseContainer[Database]{}

    "DatabaseContainer" should {
        "be thread safe" in {
            val db = Database()
            implicit val datasource = DataSourceName("", "", "")
            val result = (0 to 1000) map { _ =>
                Future {
                    Thread.sleep(100)
                    container.getOrElseUpdate {
                        Thread.sleep(100)
                        Future.successful(db.incrementAndGet())
                    }
                }.flatten
            }
            Await.ready(Future.sequence(result), Duration.Inf)
            db.count must_=== 1
        }
    }
}
