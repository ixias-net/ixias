/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import scala.language.implicitConversions

trait Box {

  /**
   * Tagged type path parameter can't be used in route specifications:
   *
   * Example route: GET /:id controllers.HomeController.testId(id: Id)
   * Compile error: class type required but Long with Tagged[IdTag] found.
   *
   * A solution to this problem is to wrap the tagged type parameter in a Function0[A]
   */
  type Box[A]    = () => A
  type BoxCsv[A] = () => Seq[A]

  implicit def    toBase[A](box: Box[A]):         A         = box()
  implicit def    toBase[A](box: BoxCsv[A]):      Seq[A]    = box()
  implicit def toBaseOpt[A](box: Option[Box[A]]): Option[A] = box.map(_())
}

