/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.persistence

import slick.jdbc.JdbcProfile
import ixias.aws.s3.backend.DataSourceName

trait SlickResource[P <: JdbcProfile] {

  implicit val driver: P

  implicit val dsn: DataSourceName

  // --[ Tables ] --------------------------------------------------------------
  object FileTable extends FileTable
  lazy val AllTables = Seq(FileTable)
}
