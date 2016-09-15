/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import ixias.play.api.auth.token.TokenViaHttpHeader

trait AuthProfileViaHttpHeader { self: AuthProfile =>

  /** The header name */
  val headerName: String = "X-IXIAS-TOKEN"

  /** The accessor for security token. */
  override lazy val tokenAccessor = TokenViaHttpHeader(headerName)
}
