/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.lifted

import scala.annotation.tailrec
import scala.language.implicitConversions
import core.domain.model.Entity
import core.port.adapter.persistence.backend.DataConverter

trait DataConvertOps {

  // Transform a record to a domain model object.
  implicit def toRecord[R, E <: Entity[_]](value: E)
    (implicit f: DataConverter[R, E]): R = f.convert(value)

  implicit def toRecord[R, E <: Entity[_]](value: Option[E])
    (implicit f: DataConverter[R, E]): Option[R] = value.map(f.convert)

  implicit def toRecord[R, E <: Entity[_]](values: Seq[E])
    (implicit f: DataConverter[R, E]): Seq[R] = toRecord(values, Seq())

  @tailrec final def toRecord[R, E <: Entity[_]](values: Seq[E], results: Seq[R])
    (implicit f: DataConverter[R, E]): Seq[R] =
    values match {
      case head :: tail => toRecord(tail, results :+ f.convert(head))
      case head +: tail => toRecord(tail, results :+ f.convert(head))
      case _ => results
    }

  // Transform a domain model object to a record.
  implicit def toModel[R, E <: Entity[_]](value: R)
    (implicit f: DataConverter[R, E]): E = f.convert(value)

  implicit def toModel[R, E <: Entity[_]](value: Option[R])
    (implicit f: DataConverter[R, E]): Option[E] = value.map(f.convert)

  implicit def toModel[R, E <: Entity[_]](values: Seq[R])
    (implicit f: DataConverter[R, E]): Seq[E] = toModel(values, Seq())

  @tailrec final def toModel[R, E <: Entity[_]](values: Seq[R], results: Seq[E])
    (implicit f: DataConverter[R, E]): Seq[E] =
    values match {
      case head :: tail => toModel(tail, results :+ f.convert(head))
      case head +: tail => toModel(tail, results :+ f.convert(head))
      case _ => results
    }
}
