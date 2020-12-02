/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.security

import ixias.util.{ Enum, Configuration }

/**
 * Evaluate the complexity of a given string
 */
object StringComplexityCheck {

  // --[ Evaluation Items ]-----------------------------------------------------
  val ADDITION_LENGTH                  =  1
  val ADDITION_ALPHA_UC                =  2
  val ADDITION_ALPHA_LC                =  3
  val ADDITION_NUMBER                  =  4
  val ADDITION_SYMBOL                  =  5
  val ADDITION_MIDDLE_NUMBER_OR_SYMBOL =  6
  val ADDITION_REQUIREMENT             =  7
  val DEDUCTION_ALPHA_ONLY             =  8
  val DEDUCTION_NUMBER_ONLY            =  9
  val DEDUCTION_REPEAT_CHAR            = 10
  val DEDUCTION_CONSECUTIVE_ALPHA_UC   = 11
  val DEDUCTION_CONSECUTIVE_ALPHA_LC   = 12
  val DEDUCTION_CONSECUTIVE_NUMBER     = 13
  val DEDUCTION_SEQUENTIAL_ALPHA       = 14
  val DEDUCTION_SEQUENTIAL_NUMBER      = 15
  val DEDUCTION_SEQUENTIAL_SYMBOL      = 16

  // --[ Data Model ]-----------------------------------------------------------
  /**
   * Evaluation result
   */
  case class Result(
    score:      Int,
    complexity: ComplexityLevel,
    evaluation: Map[Int, Evaluation]
  )

  /**
   * Evaluation item
   */
  case class Evaluation(
    val name:  String,  // Evaluation item name for display
    val rate:  Int,     // Evaluation rate
    var count: Int,     // Evaluation number of appearances
    var bonus: Int      // Evaluation bonus score
  )

  /**
   * Complecity level
   */
  sealed abstract class ComplexityLevel(val text: String) extends Enum
  object ComplexityLevel extends Enum.Of[ComplexityLevel] {
    case object IS_VERY_WEAK   extends ComplexityLevel(text = "VERY_WEAK")
    case object IS_WEAK        extends ComplexityLevel(text = "WEAK")
    case object IS_GOOD        extends ComplexityLevel(text = "GOOD")
    case object IS_STRONG      extends ComplexityLevel(text = "STRONG")
    case object IS_VERY_STRONG extends ComplexityLevel(text = "VERY_STRONG")
  }

  // --[ Check pattern ]--------------------------------------------------------
  /**
   * The pattern of charactor type
   */
  val PATTERN_CHAR_TYPE = Map(
    ADDITION_ALPHA_UC -> "[A-Z]".r,
    ADDITION_ALPHA_LC -> "[a-z]".r,
    ADDITION_NUMBER   -> "[0-9]".r,
    ADDITION_SYMBOL   -> "[^A-Za-z0-9_]".r
  )

  /**
   * The pattern of consecutive charactor
   */
  val PATTERN_CONSECUTIVE_CHAR_TYPE = Map(
    DEDUCTION_CONSECUTIVE_ALPHA_UC -> "[A-Z]".r,
    DEDUCTION_CONSECUTIVE_ALPHA_LC -> "[a-z]".r,
    DEDUCTION_CONSECUTIVE_NUMBER   -> "[0-9]".r,
  )
  /**
   * The pattern of sequential charactor
   */

  val PATTERN_SEQUENTIAL = Map(
    DEDUCTION_SEQUENTIAL_ALPHA  -> "abcdefghijklmnopqrstuvwxyz",
    DEDUCTION_SEQUENTIAL_NUMBER -> "01234567890",
    DEDUCTION_SEQUENTIAL_SYMBOL -> ")!@#$%^&*()"
  )

  /**
   * Initialized values of evaluation result
   */
  def INIT_EVALUATION = Map(
    ADDITION_LENGTH                  -> Evaluation("Number of Characters",            4, 0, 0),
    ADDITION_ALPHA_UC                -> Evaluation("Uppercase Letters",               2, 0, 0),
    ADDITION_ALPHA_LC                -> Evaluation("Lowercase Letters",               2, 0, 0),
    ADDITION_NUMBER                  -> Evaluation("Numbers",                         4, 0, 0),
    ADDITION_SYMBOL                  -> Evaluation("Symbols",                         6, 0, 0),
    ADDITION_MIDDLE_NUMBER_OR_SYMBOL -> Evaluation("Middle Numbers or Symbols",       3, 0, 0),
    ADDITION_REQUIREMENT             -> Evaluation("Requirements",                    2, 0, 0),
    DEDUCTION_ALPHA_ONLY             -> Evaluation("Letters Only",                   -4, 0, 0),
    DEDUCTION_NUMBER_ONLY            -> Evaluation("Numbers Only",                   -4, 0, 0),
    DEDUCTION_REPEAT_CHAR            -> Evaluation("Repeat Characters",              -1, 0, 0),
    DEDUCTION_CONSECUTIVE_ALPHA_UC   -> Evaluation("Consecutive Uppercase Letters",  -1, 0, 0),
    DEDUCTION_CONSECUTIVE_ALPHA_LC   -> Evaluation("Consecutive Lowercase Letters",  -2, 0, 0),
    DEDUCTION_CONSECUTIVE_NUMBER     -> Evaluation("Consecutive Numbers",            -3, 0, 0),
    DEDUCTION_SEQUENTIAL_ALPHA       -> Evaluation("Sequential Letters (3+)",       -20, 0, 0),
    DEDUCTION_SEQUENTIAL_NUMBER      -> Evaluation("Sequential Numbers (3+)",       -20, 0, 0),
    DEDUCTION_SEQUENTIAL_SYMBOL      -> Evaluation("Sequential Symbols (3+)",       -20, 0, 0)
  )

  protected val config = Configuration()
  val COMPLEXITY_MIN_LENGTH   = config.get[Option[Int]]("ixias.security.str_complexity_min_length")   .getOrElse(8)
  val COMPLEXITY_MIN_REQUIRED = config.get[Option[Int]]("ixias.security.str_complexity_min_required") .getOrElse(4)

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Check to whether A given string is complexity
   */
  def check(input: String): Result = {
    Result(
      score      = getScore(input),
      complexity = getComplexity(input),
      evaluation = calcEvaluation(input)
    )
  }

  /**
   * Calaculate complexity level of given string
   */
  def getComplexity(input: String): ComplexityLevel = {
    getScore(input) match {
      case v if v < 20 => ComplexityLevel.IS_VERY_WEAK
      case v if v < 40 => ComplexityLevel.IS_WEAK
      case v if v < 60 => ComplexityLevel.IS_GOOD
      case v if v < 80 => ComplexityLevel.IS_STRONG
      case _           => ComplexityLevel.IS_VERY_STRONG
    }
  }

  /**
   * Calaculate score of given plain password as string.
   */
  def getScore(input: String): Int = {
    val score = calcEvaluation(input).foldLeft(0)({
      case (stock, item) => stock + item._2.bonus
    })
    Math.min(Math.max(0, score), 100)
  }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Calaculate evaluation of given string
   */
  def calcEvaluation(input: String): Map[Int, Evaluation] = {
    val eva    = INIT_EVALUATION
    val tokens = input.split("")
    val length = tokens.length
    var prev   = (-1, -1)

    // Loop through string to check each charcters
    eva(ADDITION_LENGTH).count = length
    tokens.zipWithIndex.map({
      case (char, pos) => {
        // Check for Symbol, Numeric, Lowercase and Uppercase pattern matches
        PATTERN_CHAR_TYPE.foreach({
          case (evaIndex, regex) => if (regex.findFirstIn(char).isDefined) {
            eva(evaIndex).count += 1
            // Check middle number, and symbol.
            if (evaIndex == ADDITION_NUMBER || evaIndex == ADDITION_SYMBOL) {
              if (0 < pos && pos < length - 1) {
                eva(ADDITION_MIDDLE_NUMBER_OR_SYMBOL).count += 1
              }
            }
          }
        })
        // Check for consecutive Symbol, Numeric, Lowercase and Uppercase charachters
        PATTERN_CONSECUTIVE_CHAR_TYPE.foreach({
          case (evaIndex, regex) => if (regex.findFirstIn(char).isDefined) {
            if (pos == prev._1 + 1 && evaIndex == prev._2) {
              eva(evaIndex).count += 1
            }
            prev = (pos, evaIndex)
          }
        })
        // Internal loop string to check for repeated characters
        val exists = tokens.zipWithIndex.foldLeft(false)({
          case (stock, (_char, _pos)) => {
            val exists = pos != _pos && char.toLowerCase == _char.toLowerCase
            if (exists) { eva(DEDUCTION_REPEAT_CHAR).bonus += Math.abs(length / (pos - _pos)) }
            stock || exists
          }
        })
        if (exists) {
          eva(DEDUCTION_REPEAT_CHAR).count += 1
          val unique = eva(ADDITION_LENGTH).count - eva(DEDUCTION_REPEAT_CHAR).count
          if (unique > 0) {
            eva(DEDUCTION_REPEAT_CHAR).bonus =
              Math.ceil(eva(DEDUCTION_REPEAT_CHAR).bonus.toDouble / unique).toInt
          }
        }
      }
    })

    // Check for sequential alpha, numeric, symbol string patterns (forward and reverse)
    PATTERN_SEQUENTIAL.foreach({
      case (evaIndex, regex) => (0 until regex.length -3).foreach(pos => {
        val fwd = regex.slice(pos, pos + 3)
        val rev = fwd.reverse
        if (input.contains(fwd) || input.contains(rev)) {
          eva(evaIndex).count += 1
        }
      })
    })

    // Only Letters
    if (eva(ADDITION_ALPHA_UC).count + eva(ADDITION_ALPHA_LC).count == length) {
      eva(DEDUCTION_ALPHA_ONLY).count = length
    }

    // Only Numbers
    if (eva(ADDITION_NUMBER).count == length) {
      eva(DEDUCTION_NUMBER_ONLY).count = length
    }

    // Requirement
    if (eva(ADDITION_ALPHA_UC).count >  0)                     { eva(ADDITION_REQUIREMENT).count += 1 }
    if (eva(ADDITION_ALPHA_LC).count >  0)                     { eva(ADDITION_REQUIREMENT).count += 1 }
    if (eva(ADDITION_NUMBER).count   >  0)                     { eva(ADDITION_REQUIREMENT).count += 1 }
    if (eva(ADDITION_SYMBOL).count   >  0)                     { eva(ADDITION_REQUIREMENT).count += 1 }
    if (eva(ADDITION_LENGTH).count   >= COMPLEXITY_MIN_LENGTH) { eva(ADDITION_REQUIREMENT).count += 1 }

    // Calaculate Bonus
    eva.foreach({case (evaIndex, item) => evaIndex match {
      case ADDITION_REQUIREMENT => {
        if (item.count > COMPLEXITY_MIN_REQUIRED) {
          item.bonus = item.count * eva(ADDITION_LENGTH).rate
        }
      }
      case ADDITION_ALPHA_UC | ADDITION_ALPHA_LC => {
        if (item.count > 0) {
          val base   = eva(ADDITION_LENGTH).count - item.count
          item.bonus = item.rate * base
        }
      }
      case DEDUCTION_REPEAT_CHAR => item.bonus = item.rate * item.bonus
      case _                     => item.bonus = item.rate * item.count
    }})

    // Return result evaluation
    return eva
  }
}

