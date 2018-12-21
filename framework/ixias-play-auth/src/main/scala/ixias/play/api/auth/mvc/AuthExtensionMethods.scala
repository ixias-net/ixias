/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.mvc

import play.api.mvc._
import ixias.play.api.mvc.BaseExtensionMethods

trait AuthExtensionMethods extends BaseExtensionMethods { self: BaseControllerHelpers =>

  // For authentication
  def Authenticated(auth: AuthProfile[_, _, _]): ActionBuilder[Request, AnyContent] =
    AuthenticatedActionBuilder(auth, parse.default)

  // For authentication or not.
  def AuthenticatedOrNot(auth: AuthProfile[_, _, _]): ActionBuilder[Request, AnyContent] =
    AuthenticatedOrNotActionBuilder(auth, parse.default)

  // For authorization
  def Authorized[T](auth: AuthProfile[_, _, T], authority: Option[T]): ActionBuilder[Request, AnyContent] =
    AuthorizedActionBuilder(auth, authority, parse.default)
}
