/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.dbio

import java.util.ArrayDeque
import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContextExecutor, ExecutionContext }

/**
 * Contains the default ExecutionContext used by Iteratees.
 */
object Execution {

  def defaultExecutionContext: ExecutionContext = Implicits.defaultExecutionContext

  object Implicits {
    implicit def defaultExecutionContext: ExecutionContext = Execution.trampoline
    implicit def trampoline: ExecutionContextExecutor = Execution.trampoline
  }

  /**
   * Executes in the current thread.
   * Uses a thread local trampoline to make sure the stack doesn't overflow.
   */
  object trampoline extends ExecutionContextExecutor {

    /*
     * A ThreadLocal value is used to
     * track the state of the trampoline in the current thread.
     */
    private val local = new ThreadLocal[AnyRef]

    /** Marks an empty queue (see docs for `local`). */
    private object Empty

    def execute(runnable: Runnable): Unit = {
      local.get match {
        case null =>
          // Trampoline is inactive in this thread so start it up!
          try {
            local.set(Empty)
            runnable.run()
            executeScheduled()
          } finally {
            local.set(null)
          }
        case Empty =>
          // Add this Runnable to our empty queue
          local.set(runnable)
        case next: Runnable =>
          // Convert the single queued Runnable into an ArrayDeque
          // so we can schedule 2+ Runnables
          val runnables = new ArrayDeque[Runnable](4)
          runnables.addLast(next)
          runnables.addLast(runnable)
          local.set(runnables)
        case arrayDeque: ArrayDeque[_] =>
          // Add this Runnable to the end of the existing ArrayDeque
          val runnables = arrayDeque.asInstanceOf[ArrayDeque[Runnable]]
          runnables.addLast(runnable)
        case illegal =>
          throw new IllegalStateException(
            s"Unsupported trampoline ThreadLocal value: $illegal")
      }
    }

    /**
     * Run all tasks that have been scheduled in the ThreadLocal.
     */
    @tailrec
    private def executeScheduled(): Unit = {
      local.get match {
        case Empty =>
          // Nothing to run
          ()
        case next: Runnable =>
          // Mark the queue of Runnables after this one as empty
          local.set(Empty)
          next.run()
          executeScheduled()
        case arrayDeque: ArrayDeque[_] =>
          val runnables = arrayDeque.asInstanceOf[ArrayDeque[Runnable]]
          // Rather than recursing,
          // we can use a more efficient while loop.
          while (!runnables.isEmpty) {
            val runnable = runnables.removeFirst()
            runnable.run()
          }
        case illegal =>
          throw new IllegalStateException(
            s"Unsupported trampoline ThreadLocal value: $illegal")
      }
    }

    def reportFailure(t: Throwable): Unit = t.printStackTrace()
  }
}
