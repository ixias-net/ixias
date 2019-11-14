/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import ixias.model._
import ixias.security.RandomStringToken
import ixias.aws.s3.backend.DataSourceName

// To build file a object
//~~~~~~~~~~~~~~~~~~~~~~~~~
trait FileBuilder[S <: @@[_, _], M <: AnyRef] { self =>

  type SiloBaseId = S
  type SiloBase   = FileSilo[S]
  type Model      = M

  // --[ Properties ]-----------------------------------------------------------
  /** Silo configuration */
  val siloBase:      SiloBase

  /** Feature name */
  val fileNamespace: String

  /** Prefix of filename */
  val filePrefix:    String

  /** directory seperator as string */
  val DIRECTORY_SEPARATOR = """/"""

  // --[ Methods ]------------------------------------------------------------
  /**
   * Get a base-id for silo generation.
   */
  protected def siloBaseId(base: Model): SiloBaseId

  /**
   * Get a key-name of the file in the storage destination.
   */
  protected def build(namespace: String, fname: String, base: Model): String = (
    namespace
      :: siloBase.silo(self.siloBaseId(base))
      :: fname
      :: Nil
  ).mkString(DIRECTORY_SEPARATOR)

  // --[ Methods ]------------------------------------------------------------
  /**
   * Get a key-name of the file in the storage destination.
   */
  final def apply(base: Model, typedef: String = "unknown")
    (implicit dsn: DataSourceName): File#WithNoId =
    (for {
      fkey <- scala.util.Try(build(
        fileNamespace,
        "%s-%s".format(filePrefix, RandomStringToken.next(64)),
        base
      ))
      file <- File(fkey, typedef, None)
    } yield file) getOrElse {
      val message = "Failed to create file object. dsn, baseModel = %s".format(dsn, base)
      throw new IllegalStateException(message)
    }
}

// To build file a object
//~~~~~~~~~~~~~~~~~~~~~~~~~
trait FileBuilderExt[S1 <: @@[_, _], S2 <: @@[_, _], M <: AnyRef] extends FileBuilder[S2, M] { self =>

  type SiloRootId = S1
  type SiloRoot   = FileSilo[S1]

  // --[ Properties ]-----------------------------------------------------------
  /** Silo configuration */
  val siloRoot: SiloRoot

  // --[ Methods ]------------------------------------------------------------
  /**
   * Get a root-id for silo generation.
   */
  protected def siloRootId(root: Model): SiloRootId

  /**
   * Get a key-name of the file in the storage destination.
   */
  override protected def build(namespace: String, fname: String, base: Model): String = (
    siloRoot.silo(self.siloRootId(base))
      :: namespace
      :: siloBase.silo(self.siloBaseId(base))
      :: fname
      :: Nil
  ).mkString(DIRECTORY_SEPARATOR)

}
