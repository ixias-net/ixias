/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import scala.util.{ Try, Success, Failure }
import core.port.adapter.persistence.backend.Protocol

/** The data source object. */
case class DataSource(
  private var path:     String,
  private var protocol: Protocol       = Protocol.TCP,
  private var socket:   Option[String] = None,
  private var hostspec: Option[String] = None,
  private var port:     Option[Int]    = None,
  private var database: Option[String] = None,
  private var user:     Option[String] = None,
  private var password: Option[String] = None,
  private var options:  Map[String, String] = Map.empty
) {

  // The Path of typesafe config
  def getPath(): String              = this.path
  def setPath(v: String)             = this.path = v

  // Protocol
  def getProtocol(): Protocol        = this.protocol
  def setProtocol(v: Protocol)       = this.protocol = v

  // The path of socket
  def getSocket(): Option[String]    = this.socket
  def setSocket(v: Option[String])   = this.socket = v
  def setSocket(v: String)           = this.socket = Some(v)

  // Hostspec
  def getHostSpec(): Option[String]  = this.hostspec
  def setHostSpec(v: Option[String]) = this.hostspec = v
  def setHostSpec(v: String)         = this.hostspec = Some(v)

  // Port
  def getPort(): Option[Int]         = this.port
  def setPort(v: Option[Int])        = this.port = v
  def setPort(v: Int)                = this.port = Some(v)

  // Database
  def getDatabase(): Option[String]  = this.database
  def setDatabase(v: Option[String]) = this.database = v
  def setDatabase(v: String)         = this.database = Some(v)

  // User
  def getUser(): Option[String]      = this.user
  def setUser(v: Option[String])     = this.user = v
  def setUser(v: String)             = this.user = Some(v)

  // Password
  def getPassword(): Option[String]  = this.password
  def setPassword(v: Option[String]) = this.password = v
  def setPassword(v: String)         = this.password = Some(v)

  // Options
  def getOptions(): Map[String, String]  = this.options
  def  addOption(v: (String, String))    = this.options += v
  def setOptions(v: Map[String, String]) = this.options  = v

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  path:     $path,
        |  protocol: $protocol
        |  socket:   $socket
        |  hostspec: $hostspec
        |  port:     $port
        |  database: $database
        |  user:     $user
        |  password: $password
        |  options:  $options
        |}""".stripMargin
}

/** The factory for data source object. */
object DataSource {
  def forName(name: String): Try[DataSource] = ???
}
