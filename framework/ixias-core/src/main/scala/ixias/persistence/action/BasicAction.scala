/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.concurrent.Future
import ixias.persistence.dbio.{ IOActionContext, EntityIOActionContext }

/**
 * A builder for generic DB Actions that generalizes over the type of requests.
 */
trait BasicActionFunction[-R, +P] {

  protected implicit val IOActionContext = EntityIOActionContext.Implicits.global

  /**
   * Invoke the block.
   * This is the main method that an ActionBuilder has to implement.
   */
  def invokeBlock[A](request: R, block: P => Future[A]): Future[A]

}

trait BasicAction[-R, +P] extends BasicActionFunction[R, P]
