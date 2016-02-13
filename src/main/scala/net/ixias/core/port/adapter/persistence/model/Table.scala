/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.model

import slick.driver.JdbcProfile
import core.port.adapter.persistence.lifted._

trait Table[R <: TableRecord, P <: JdbcProfile] { self =>

  /** The type of all table row objects. */
  type Table = driver.Table[R]

  /** The configured driver. */
  val driver: P

  /** The all table row objects. */
  val table: Table

  /** The API for using the utility methods with a single import statement.
    * This provides the repository's implicits, the Database connections,
    * and commonly types and objects. */
  trait API extends driver.API
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P] {
    lazy val driver = self.driver
  }

  val api: API = new API {}
}
