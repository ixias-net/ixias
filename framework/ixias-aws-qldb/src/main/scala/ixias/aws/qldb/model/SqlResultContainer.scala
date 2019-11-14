/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb.model

/**
 * Container definition with original execution result and conversion information
 */
trait SqlResultContainer {

  /**
   * Type of model of mapping destination
   */
  type Model

  /**
   * Type of Result
   */
  type Result

  /**
   * Get result value.
   */
  def value: Result

  /**
   * Raw results of Amazon QLDB
   */
  val data: software.amazon.qldb.Result
}

