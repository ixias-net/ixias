/*
 * This file is part of the ixias services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

/** Aliases for lifted embedding features. This trait can be mixed into aliasing
  * objects which simplify the use of the lifted embedding. */
trait Aliases {

  /** DDD Model */
  val SomeId   = ixias.model.SomeId
  val NoneId   = ixias.model.NoneId
  val Identity = ixias.model.Identity

  /** HTTP */
  val Json     = ixias.play.api.mvc.Json
  val Jade     = ixias.play.api.mvc.Jade
  val Form     = ixias.play.api.mvc.FormAction
}
