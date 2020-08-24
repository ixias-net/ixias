/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import shapeless.{ HList, LabelledGeneric, Poly2 }
import shapeless.ops.hlist._
import shapeless.ops.record._

/**
 * Merger: model to model with using `LabelledGeneric`
 */
trait LabelledGenericMerger[T, U] {

  object mf extends Poly2 {
    implicit def opt1[A] = at[A, A]                 ((v1, v2) => v2)
    implicit def opt2[A] = at[Option[A], Option[A]] ((v1, v2) => v2 orElse v1)
  }

  /**
   * Copies all enumerable own properties
   * from source object to a target object.
   */
  def merge[RT <: HList, RU <: HList](data1: T, data2: U, hard: Boolean = false)(implicit
    lgenT:     LabelledGeneric.Aux[T, RT],
    lgenU:     LabelledGeneric.Aux[U, RU],
    merger:    Merger.Aux[RT, RU, RT],
    mergeWith: MergeWith.Aux[RT, RU, mf.type, RT]
  ): T = lgenT.from(hard match {
    case true  =>    merger(lgenT.to(data1), lgenU.to(data2))
    case false => mergeWith(lgenT.to(data1), lgenU.to(data2))
  })

  /**
   * Copies all enumerable own properties
   * from source generic values to a target object.
   */
  def mergeGen[RT <: HList, RU <: HList, RN <: HList](data: T, gen: RU, hard: Boolean = false)(implicit
    lgenT:        LabelledGeneric.Aux[T, RT],
    intersection: Intersection.Aux[RU, RT, RN],
    merger:       Merger.Aux[RT, RN, RT],
    mergeWith:    MergeWith.Aux[RT, RN, mf.type, RT]
  ): T = lgenT.from(hard match {
    case true  =>    merger(lgenT.to(data), gen.intersect[RT])
    case false => mergeWith(lgenT.to(data), gen.intersect[RT])
  })
}
