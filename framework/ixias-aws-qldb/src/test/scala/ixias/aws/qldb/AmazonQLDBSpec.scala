/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import org.specs2.mutable._
// import scala.concurrent.duration.Duration
// import scala.concurrent.{Await, Future}
// import scala.concurrent.ExecutionContext.Implicits.global
import ixias.persistence.model.DataSourceName

class AmazonQLDBSpec extends Specification {
  "AmazonQLDBSpec" should {
    "Create Session for AmazonQLDB" in {
      implicit val dsn = DataSourceName("ixias.aws.qldb://ledger/test")
      val session = AmazonQLDBBackend.getDatabase
      println(dsn)
      true must_== true
    }
  }
}
