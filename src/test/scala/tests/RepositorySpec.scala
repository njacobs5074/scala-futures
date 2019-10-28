package tests

import java.util.Date
import java.util.concurrent.Executors

import model._
import org.scalatest.Matchers._
import org.scalatest._
import repository.Repository

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.postfixOps

/**
 * Tests to confirm that serialized access to the repository will produce the correct result.
 */
class RepositorySpec extends FlatSpec {

  "Repository" should "insert DAO" in {

    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5)))

    // Simulate single-threaded access to the repo from a caller.
    Await.result(repo.insert(DAObject[Int, String](id = 1, data = "Hello", created = new Date())), 500 milliseconds)

    assert(repo.repo.get(1).exists(_.data == "Hello"))
  }

  it should "update a DAO" in {
    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5)))

    val (t1, t2) = getSuccessiveTimestamps
    val recordId = 1
    val dao = new DAObject[Int, String](id = recordId, data = "Hello", created = t1)
    val updatedDao = dao.copy(data = "Goodbye", lastUpdated = Some(t2))

    // Again, we simulate a caller who invokes us in serial fashion by awaiting each repo operation.
    Await.result(repo.insert(dao), 500 milliseconds)
    Await.result(repo.update(updatedDao), 500 milliseconds)

    // Confirm that the 2nd update is one in the database.
    assert(repo.repo.get(recordId).exists(_.data == "Goodbye"))
    assert(repo.repo.get(recordId).exists(_.lastUpdated.contains(t2)))
  }
}
