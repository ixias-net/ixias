/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package play.api.auth.mvc

import play.api.mvc.StackAction._
import play.api.auth.AuthProfile

/** Provides the utility methods for constructing Action.
  * An action is essentially a (Request[A] => Result) function that
  * handles a request and generates a result to be sent to the client. */
trait Action { self: AuthProfile =>

  /** The key of attribute for containing required authority roles. */
  case object UserKey extends StackRequest.AttributeKey[User]

  /** The key of attribute for containing user data. */
  case object AuthorityKey extends StackRequest.AttributeKey[Authority]
}
