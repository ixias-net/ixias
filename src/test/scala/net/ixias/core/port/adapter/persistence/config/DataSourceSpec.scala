/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import org.specs2.mutable.Specification
import core.port.adapter.persistence.io.EntityIOActionContext.Implicits.global

class DataSourceSpec extends Specification {
  "DataSource" should {
    "forName" in {
      val exists = 1
      object SlickDataSource extends SlickDataSource
      val a = SlickDataSource.DataSource.forDSN("slick.db://master/test")
      println(a)

      // val source1 = DataSource.forName("slick.db://slave/kidsna_udb")
      // val source2 = DataSource.forName("slick.db://user:password@slave/kidsna_udb")
      // val source3 = DataSource.forName("slick.db://user:password@slave:103306/kidsna_udb")
      // val source4 = DataSource.forName("slick.db://user:password@slave")
      // val source5 = DataSource.forName("slick.db://user:password@udp(127.0.0.1:9000)/kidsna_udb")
      // val source6 = DataSource.forName("slick.db://user:password@unix(/var/tmp/mysql.sock)/kidsna_udb?var1=xxxx&var2=zzzz")
      // val source7 = DataSource.forName("slick.db://user:password@unix(/var/tmp/mysql.sock)")
      // println(source1)
      // println(source2)
      // println(source3)
      // println(source4)
      // println(source5)
      // println(source6)
      // println(source7)
      exists must_== 1
    }
  }
}
