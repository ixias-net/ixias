/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.io

import com.typesafe.config.{ Config, ConfigFactory }

/** The context object passed to database actions by the repository. */
case class EntityIOActionContext(
  val config: Config
) extends IOActionContext

object EntityIOActionContext {

  /** The explicit global `EntityIOActionContext`.
    * Invoke `global` when you want to provide the global `EntityIOActionContext` explicitly. */
  def global: EntityIOActionContext = Implicits.global

  object Implicits {
    /** The implicit global `EntityIOActionContext`.
      * Import `global` when you want to provide the global `EntityIOActionContext` implicitly. */
    implicit lazy val global: EntityIOActionContext = EntityIOActionContext(ConfigFactory.load())
  }
}
