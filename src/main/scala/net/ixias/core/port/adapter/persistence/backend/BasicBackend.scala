/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence.backend

import port.adapter.persistence.io.IOActionContext

trait BasicBackend extends DatabaseComponent {

  // --[ TypeDefs ]-------------------------------------------------------------
  /** The type of myself*/
  type This = BasicBackend
  /** The type of database source config used by this backend. */
  type DatabaseSouceConfig = DatabaseSouceConfigDef
  /** The type of the database souce config factory used by this backend. */
  type DatabaseSouceConfigFactory = DatabaseSouceConfigFactoryDef
  /** The type of the context used for running repository Actions */
  type Context = RepositoryIOActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DatabaseSouceConfig = new DatabaseSouceConfigFactory{}

  // --[ DatabaseSouceConfigDef ] ----------------------------------------------
  case class DatabaseSouceConfigDef(
    val path:     String,
    val protocol: Protocol       = Protocol.Unknown,
    val socket:   Option[String] = None,
    val hostspec: Option[String] = None,
    val port:     Option[Int]    = None,
    val database: Option[String] = None,
    val user:     Option[String] = None,
    val password: Option[String] = None,
    val options:  Map[String, String] = Map.empty
  ) extends super.DatabaseSouceConfigDef {
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
      var conf = scala.collection.mutable.Map(
        DatabaseSouceConfigDef.getClass.getDeclaredFields.map(_.getName)
          .zip(DatabaseSouceConfigDef(path = tok).productIterator.toList).toSeq: _*)

      // PART :: user:password.
      tail.indexOf("@") match { case -1 => case p1 =>
        tok  = tail.substring(0, p1)
        tail = tail.substring(p1 + 1)
        tok.indexOf(":") match {
          case -1 => conf("user")     = Some(tok)
          case p2 => conf("user")     = Some(tok.substring(0, p2))
                     conf("password") = Some(tok.substring(p2 + 1))
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
        conf("hostspec") = Some(hostspec)
        conf("protocol") = protocol
        hostspec.indexOf(":") match {
          case -1 => conf("hostspec") = Some(hostspec)
          case  p => conf("hostspec") = Some(hostspec.substring(0, p))
                     conf("port")     = catching(classOf[Exception]) opt hostspec.substring(p + 1).toInt
        }
        if (protocol == Protocol.Unix) {
          conf("socket")   = conf("hostspec")
          conf("hostspec") = None
        }
      }
      // PART :: database
      if (0 < tail.length) {
        val regex = """^(.+)=(.+)$""".r
        tail.lastIndexOf("?") match {
          case -1 => conf("database") = Some(tail)
          case  p => conf("database") = Some(tail.substring(0, p))
                     conf("options")  = tail.substring(p + 1).split("&").map{ case regex(k, v) => (k -> v) }
        }
      }
      // Instantiate an object.
      DatabaseSouceConfigDef.getClass.getMethods
        .find(_.getName == "apply").get.invoke(DatabaseSouceConfigDef,
          conf.values.toList.map(_.asInstanceOf[AnyRef]): _*).asInstanceOf[DatabaseSouceConfigDef]
    }
  }

  /** The context object passed to database actions by the repository. */
  case class RepositoryIOActionContext(
    val conf: com.typesafe.config.Config
  ) extends IOActionContext
}
