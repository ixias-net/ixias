/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.io

import com.typesafe.config.Config

/** The context object passed to database actions by the repository. */
trait EntityIOActionContext extends IOActionContext {
  val conf: Config
}
