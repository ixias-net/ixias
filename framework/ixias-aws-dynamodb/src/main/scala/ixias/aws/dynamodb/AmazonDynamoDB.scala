/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.dynamodb

import ixias.persistence.Profile
import ixias.aws.dynamodb.model.Schema
import ixias.aws.dynamodb.backend.{ AmazonDynamoDBBackend, AmazonDynamoDBMapper }
import scala.concurrent.Future

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper

trait AmazonDynamoDBProfile extends Profile {

  // --[ Typedefs ]-------------------------------------------------------------
  /** The type of database objects. */
  type Database = AmazonDynamoDB

  /** The back-end type required by this profile */
  type Backend  = AmazonDynamoDBBackend.type

  // --[ Properties ]-----------------------------------------------------------
  /** The back-end implementation for this profile */
  protected lazy val backend = AmazonDynamoDBBackend

  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends super.API
  val api: API = new API {}

  // --[ Methods ]-----------------------------------------------------------
  /**
   * Execute database action.
   */
  def RunDBAction[A, M, S <: Schema[M]](schema: S)(block: AmazonDynamoDBMapper[M, S] => A): Future[A] =
    for {
      client <- backend.getDatabase(schema.dsn)
      mapper  = AmazonDynamoDBMapper[M, S](schema, new DynamoDBMapper(client))
      result <- Future(block(mapper))
    } yield result
}
