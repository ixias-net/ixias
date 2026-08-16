/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend.memcached

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/**
 * The configuration to connect a memcached cluster.
 *
 * @param addresses        the server addresses, separated by a space or a comma. (ex: "127.0.0.1:11211")
 * @param keysPrefix       the prefix prepended to every key, so that several applications
 *                         can share the same memcached instances without stepping over each other.
 * @param operationTimeout the limit after which an operation finishes with `Failure(TimeoutException)`.
 */
case class Configuration(
  addresses:        String,
  keysPrefix:       Option[String] = None,
  operationTimeout: FiniteDuration = FiniteDuration(1, TimeUnit.SECONDS)
)
