/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import scala.concurrent.ExecutionContext

trait BaseExtensionMethods {

  /** HTTP Request/Response Helper */
  val Json = ixias.play.api.mvc.JsonAction
  val Form = ixias.play.api.mvc.FormAction
//val Jade = ixias.play.api.mvc.JadeAction

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
