/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.backend

import scala.language.reflectiveCalls
import core.port.adapter.persistence.io.EntityIOActionContext

trait BasicBackend extends DatabaseComponent {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of database source config used by this backend. */
  type DatabaseSouceConfig = DatabaseSouceConfigDef
  /** The type of the database souce config factory used by this backend. */
  type DatabaseSouceConfigFactory = DatabaseSouceConfigFactoryDef
  /** The type of the context used for running repository Actions */
  type Context = EntityIOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DatabaseSouceConfig = new DatabaseSouceConfigFactory{}

  // --[ DatabaseSouceConfigDef ] ----------------------------------------------
  trait DatabaseSouceConfigDef extends super.DatabaseSouceConfigDef {
    val path:     String
    val protocol: Protocol
    val socket:   Option[String]
    val hostspec: Option[String]
    val port:     Option[Int]
    val database: Option[String]
    val user:     Option[String]
    val password: Option[String]
    val options:  Map[String, String]

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

  // --[ DatabaseSouceConfigFactoryDef ] ---------------------------------------
  /** The database souce config factory */
  trait DatabaseSouceConfigFactoryDef extends super.DatabaseSouceConfigFactoryDef {
    this: DatabaseSouceConfigFactory =>
    /** Load a configuration for persistent database. */
    def forDSN(name: String): DatabaseSouceConfig = {
      import scala.util.control.Exception._

      // Create a mutable temporary conf values.
      var (tok, tail) = name.indexOf("://") match {
        case -1 => throw new Exception(s"""Dose not match the DSN format. ($name)""")
        case  p => (name.substring(0, p), name.substring(p + 3))
      }
      var conf = new {
        var path:     String         = tok
        var protocol: Protocol       = Protocol.Unknown
        var socket:   Option[String] = None
        var hostspec: Option[String] = None
        var port:     Option[Int]    = None
        var database: Option[String] = None
        var user:     Option[String] = None
        var password: Option[String] = None
        var options:  Map[String, String] = Map.empty
      }

      // PART :: user:password.
      tail.indexOf("@") match { case -1 => case p1 =>
        tok  = tail.substring(0, p1)
        tail = tail.substring(p1 + 1)
        tok.indexOf(":") match {
          case -1 => conf.user     = Some(tok)
          case p2 => conf.user     = Some(tok.substring(0, p2))
                     conf.password = Some(tok.substring(p2 + 1))
        }
      }
      // PART :: hostspec or proto(proto_opts)
      tail.lastIndexOf("/") match {
        case -1 => tok = tail; tail = ""
        case  p => tok = tail.substring(0, p); tail = tail.substring(p + 1)
      }
      if (0 < tok.length) {
        val  regex = """^([^(]+)\((.*?)\)$""".r
        val (hostspec, protocol) = tok match {
          case regex("udp",  h) => (h, Protocol.UDP)
          case regex("unix", h) => (h, Protocol.Unix)
          case regex(_,      h) => (h, Protocol.Unknown)
          case h                => (h, Protocol.Unknown)
        }
        conf.hostspec = Some(hostspec)
        conf.protocol = protocol
        hostspec.indexOf(":") match {
          case -1 => conf.hostspec = Some(hostspec)
          case  p => conf.hostspec = Some(hostspec.substring(0, p))
                     conf.port     = catching(classOf[Exception]) opt hostspec.substring(p + 1).toInt
        }
        if (protocol == Protocol.Unix) {
          conf.socket   = conf.hostspec
          conf.hostspec = None
        }
      }
      // PART :: database
      if (0 < tail.length) {
        val regex = """^(.+)=(.+)$""".r
        tail.lastIndexOf("?") match {
          case -1 => conf.database = Some(tail)
          case  p => conf.database = Some(tail.substring(0, p))
            tail.substring(p + 1).split("&").map{ case regex(k, v) => conf.options += (k -> v) }
        }
      }
      new DatabaseSouceConfigDef {
        val path     = conf.path
        val protocol = conf.protocol
        val socket   = conf.socket
        val hostspec = conf.hostspec
        val port     = conf.port
        val database = conf.database
        val user     = conf.user
        val password = conf.password
        val options  = conf.options
      }
    }
  }
}
