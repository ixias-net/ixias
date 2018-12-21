/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.concurrent.Future

/**
 * A builder for generic DB Actions that generalizes over the type of requests.
 */
trait BasicActionFunction[-R, +T] {

  /**
   * Invoke the block.
   * This is the main method that an ActionBuilder has to implement.
   */
  def invokeBlock[A](request: R, block: T => Future[A]): Future[A]
}

trait BasicAction[-R, +T] extends BasicActionFunction[R, T]
