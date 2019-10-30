import java.util.{Calendar, Date}
import java.util.concurrent.Executors

import monix.execution.Scheduler

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

package object tests {

  /** Get 2 dates that are 1 second apart. */
  def getSuccessiveTimestamps: (Date, Date) = {
    val c = Calendar.getInstance()
    val t1 = c.getTime
    c.add(Calendar.SECOND, 1)
    val t2 = c.getTime
    (t1, t2)
  }

  /** Execution context used by the repo in our tests */
  def newRepoExecutionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  /** Execution context used by our future.service in the tests */
  def newServiceExecutionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  /** Monix Schedulers */
  def repoScheduler: Scheduler = Scheduler.apply(newRepoExecutionContext)
  def serviceScheduler: Scheduler = Scheduler.apply(newServiceExecutionContext)
}
