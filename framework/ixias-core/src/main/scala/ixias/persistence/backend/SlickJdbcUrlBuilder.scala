/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.util.{ Try, Success, Failure }
import ixias.persistence.model.DataSourceName

/**
 * Provide a feature to build JDBC-Url.
 */
trait SlickJdbcUrlBuilder {

  /**
   * Generate a url for JDBC connection resouce.
   */
  def buildUrl(implicit dsn: DataSourceName): Try[String]
}

/**
 * The provider for SlickJdbcUrlBuilder
 */
object SlickJdbcUrlBuilderProvider {

  /**
   * Registered SlickJdbcUrlBuilders.
   */
  private var stock: Map[String, SlickJdbcUrlBuilder] = Map(
    "com.mysql.jdbc.Driver"           -> new SlickJdbcUrlBuilderForMySQL(),
    "com.amazon.redshift.jdbc.Driver" -> new SlickJdbcUrlBuilderForRedshift()
  )

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Retrive a UrlBuilder from registered stack.
   */
  def resolve(driver: String): Try[SlickJdbcUrlBuilder] =
    stock.get(driver) match {
      case Some(v) => Success(v)
      case None    => Failure(new IllegalArgumentException(
        "Could not resolve the JDBC vendor format. %s".format(driver)
      ))
    }

  /**
   * Register a UrlBuilder to stack.
   */
  def register(driverName: String, builder: SlickJdbcUrlBuilder): Unit =
    this.stock = this.stock + (driverName -> builder)
}
