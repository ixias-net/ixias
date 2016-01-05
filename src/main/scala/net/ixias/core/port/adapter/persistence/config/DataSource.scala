/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.config

import scala.util.{ Try, Success, Failure }
import core.util.EnumOf
import core.port.adapter.persistence.backend.Protocol

/** The data source object. */
case class DataSource(
  var path:     String,
  var protocol: Option[Protocol] = None,
  var socket:   Option[String]   = None,
  var hostspec: Option[String]   = None,
  var port:     Option[Int]      = None,
  var database: Option[String]   = None,
  var user:     Option[String]   = None,
  var password: Option[String]   = None,
  var options:  Map[String, String] = Map.empty
) {
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
  /** build a `DataSource` object by DSN as string.
    * DNS example :
    *   - slick.db://master/database1
    *   - slick.db://user:password@master/database1
    *   - slick.db://user:password@127.0.0.1:103306/database1
    *   - slick.db://user:password@udp(127.0.0.1:103306)/database1
    *   - slick.db://user:password@unix(/var/tmp/mysql.sock)
    */
  def forName(name: String): Try[DataSource] = {
    // Create a data souce object.
    var (source, tail) = name.indexOf("://") match {
      case -1  => throw new Exception(s"""Dose not match the DSN format. ($name)""")
      case pos => (DataSource(path = name.substring(0, pos)), name.substring(pos + 3))
    }
    // PART -> user:password.
    tail.indexOf("@") match { case -1 => case pos =>
      val tok = tail.substring(0, pos)
      tok.indexOf(":") match {
        case -1  => source.user     = Some(tok)
        case pos => source.user     = Some(tok.substring(0, pos))
                    source.password = Some(tok.substring(pos + 1))
      }
      tail = tail.substring(pos + 1)
    }
    // PART(left)  -> hostspec or proto(proto_opts)
    // PART(right) -> database and options
    var (left, right, closed, separator) = ("", "", false, false)
    tail.map(_ match {
      case '/' if !closed => separator = true
      case c => {
        if (c == '(') closed = true
        if (c == ')') closed = false
        if (!separator) left += c else right += c
      }
    })
    // PART(left) -> hostspec or proto(proto_opts)
    if (0 < left.length) {
      val regex = """^(.+?)\((.*)\)$""".r
      val (hostspec, protocol) = left match {
        case regex("udp",  h) => (h, Protocol.UDP)
        case regex("unix", h) => (h, Protocol.Unix)
        case regex(_,      h) => (h, Protocol.Unknown)
        case h                => (h, Protocol.TCP)
      }
      source.protocol = Some(protocol)
      source.protocol.map(_ match {
        case Protocol.Unix => source.socket = Some(hostspec)
        case _ => hostspec.indexOf(":") match {
          case  -1 => source.hostspec = Some(hostspec)
          case pos => source.hostspec = Some(hostspec.substring(0, pos))
                      source.port     = Some(hostspec.substring(pos + 1).toInt)
        }
      })
    }
    // PART(right) -> database and options
    if (0 < right.length) {
      val regex = """^(.+)=(.+)$""".r
      right.lastIndexOf("?") match {
        case  -1 => source.database = Some(right)
        case pos => source.database = Some(right.substring(0, pos))
          right.substring(pos + 1).split("&").map {
            case regex(k, v) => source.options += (k -> v) }
      }
    }
    Success(source)
  }
}
