/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import scala.concurrent.ExecutionContext

trait BaseExtensionMethods {

  /** DDD Model */
  val SomeId   = ixias.model.SomeId
  val NoneId   = ixias.model.NoneId
  val Identity = ixias.model.Identity

  /** HTTP */
  val Json     = ixias.play.api.mvc.Json
  val Jade     = ixias.play.api.mvc.Jade
  val Form     = ixias.play.api.mvc.FormAction

  /**
   * The execution context to run this action in
   */
  def executionContext: ExecutionContext =
    play.api.libs.concurrent.Execution.defaultContext

  object Implicits {
    implicit def defaultContext: ExecutionContext =
      play.api.libs.concurrent.Execution.defaultContext
  }
}
