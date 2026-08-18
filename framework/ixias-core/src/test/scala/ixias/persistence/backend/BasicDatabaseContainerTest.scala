package ixias.persistence.backend

import ixias.persistence.model.DataSourceName
import org.specs2.mutable._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

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
            implicit val datasource: DataSourceName = DataSourceName("", "", "")
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

        "retry a data source that failed to be created" in {
            implicit val datasource: DataSourceName = DataSourceName("ixias.db.mysql", "master", "failed")
            val failure = container.getOrElseUpdate(Future.failed(new RuntimeException("Database is not available.")))
            Await.ready(failure, Duration.Inf)

            val db = Database()
            Await.result(container.getOrElseUpdate(Future.successful(db)), Duration.Inf) must_=== db
        }

        "retry a data source whose creation failed asynchronously" in {
            implicit val datasource: DataSourceName = DataSourceName("ixias.db.mysql", "master", "failed_async")
            Await.ready(container.getOrElseUpdate(Future {
                Thread.sleep(100)
                throw new RuntimeException("Database is not available.")
            }), Duration.Inf)

            // The callback evicting a data source that is still being created may
            // run a moment after `Await` returns, so the first retry can still see
            // the failure -- which then evicts it on the spot.
            val db     = Database()
            val result = (1 to 10).iterator.map { _ =>
                Try(Await.result(container.getOrElseUpdate(Future.successful(db)), Duration.Inf)).toOption
            }.collectFirst { case Some(value) => value }
            result must beSome(db)
        }
    }
}
