/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.Try
import ixias.persistence.model.DataSourceName

/**
 * Build a JDBC-Url for Redshift.
 */
class SlickJdbcUrlBuilderForRedshift extends SlickJdbcUrlBuilder with BasicDatabaseConfig {

  // --[ Properties ]-----------------------------------------------------------
  val FMT_URL_DEFALT = """jdbc:postgresql://%s/%s?currentSchema=%s"""

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Generate a url for JDBC connection resouce.
   */
  def getUrl(implicit dsn: DataSourceName): Try[String] =
    for {
      hosts    <- getHosts
      database <- getDatabaseName
    } yield {
      FMT_URL_DEFALT.format(hosts.mkString(","), database, getSchemaName)
    }
}
