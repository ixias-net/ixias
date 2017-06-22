/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

import scala.concurrent.Future
import scala.language.higherKinds
import scala.concurrent.ExecutionContext
import org.slf4j.LoggerFactory
import ixias.util.Logger
import ixias.model.{ Tagged, Entity, IdStatus }

/**
 * An Entity Action that can be executed on a persistence database.
 */
trait EntityIOAction[K <: Tagged[_, _], E[S <: IdStatus] <: Entity[K, S]] extends IOAction {

  /** The type of entity id */
  type Id = K

  /** The type of entity */
  type Entity[S <: IdStatus] = E[S]

  /** The type of entity */
  type EntityEmbeddedId = E[IdStatus.Exists]

  /** The type of entity when it has not id. */
  type EntityWithNoId   = E[IdStatus.Empty]

  /** The Execution Context */
  protected implicit val ctx: ExecutionContext = Execution.Implicits.trampoline

  /** The logger for profile */
  protected lazy val logger = new Logger(LoggerFactory.getLogger(this.getClass.getName))

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Defines the default value computation for the map,
   * returned when a identity is not found The method implemented here throws an exception,
   * but it might be overridden in subclasses.
   */
  def default(id: Id): Future[EntityEmbeddedId] =
    Future.failed(new NoSuchElementException("identity not found: " + id))

  /**
   * Retrieves the value which is associated with the given identity.
   * This method invokes the `default` method of the map if there is no mapping
   * from the given identity to a value.
   */
  def apply(id: Id): Future[EntityEmbeddedId] =
    get(id).flatMap(_ match {
      case Some(v) => Future.successful(v)
      case None    => default(id)
    })

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Optionally returns the value associated with a identity.
   */
  def get(id: Id): Future[Option[EntityEmbeddedId]]

  /**
   * Returns the value associated with a identity, or
   * a default value if the identity is not contained in the repository.
   */
  def getOrElse[E2 >: EntityEmbeddedId](id: Id, f: Id => E2): Future[E2] =
    get(id).map(_.getOrElse(f(id)))

  /**
   * Tests whether this repository contains a binding for a identity.
   */
  def contains(id: Id): Future[Boolean] =
    get(id).map(_.isDefined)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Adds a new identity/entity-value pair to this repository.
   * If the map already contains a mapping for the identity,
   * it will be overridden by the new value
   */
  def store(entity: Entity[_]): Future[Id]

  /**
   * Removes a identity from this map,
   * returning the value associated previously with that identity as an option.
   */
  def remove(id: Id): Future[Option[EntityEmbeddedId]]
}
