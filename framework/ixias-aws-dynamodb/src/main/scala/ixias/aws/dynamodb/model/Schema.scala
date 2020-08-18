/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.dynamodb.model

import ixias.persistence.model.DataSourceName

/**
 * Represent schema model of Amazon DynamoDB.
 */
trait Schema[M] {

  /**
   * The map of DSN as string.
   */
  val dsn: DataSourceName

  /**
   * The schema deshinition
   */
  type Model <: Any

  /**
   * Converter from raw object to immutable model object
   */
  val convert: shapeless.Generic[M] {
    type Repr = Model
  }
}
