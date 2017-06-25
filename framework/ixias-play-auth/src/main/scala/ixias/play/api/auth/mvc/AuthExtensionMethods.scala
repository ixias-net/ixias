/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

trait AuthExtensionMethods {
  val Authenticated      = AuthenticatedActionBuilder
  val AuthenticatedOrNot = AuthenticatedOrNotActionBuilder
  val Authorized         = AuthorizedActionBuilder
}
