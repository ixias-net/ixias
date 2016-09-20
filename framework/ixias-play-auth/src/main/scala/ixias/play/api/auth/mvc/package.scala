/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth

import ixias.play.api.mvc.StackActionRequest

package object mvc {

  /** The key of attribute for containing required auth profile. */
  case object AuthProfileKey extends StackActionRequest.AttributeKey[AuthProfile]
}
