package tests

import java.util.Date

import dto.DTO
import org.scalatest.AsyncFlatSpec
import repository.Repository
import service.Service

import scala.async.Async.{ async, await }
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

class AsyncAwaitServiceSpec extends AsyncFlatSpec {

  "Service" should "add new item" in async {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val t1 = new Date()
    await(service.createOrUpdate(DTO(1, "hello", t1)))

    assert(repo.repo.get(1).exists(dao => dao.id == 1 && dao.data == "hello" && dao.lastUpdated.isEmpty))
  }

  it should "fail if updates run in parallel" in async {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    val f1 = service.createOrUpdate(DTO(1, "hello", t1))
    val f2 = service.createOrUpdate(DTO(1, "goodbye", t2))

    await(Future.sequence(List(f1, f2)).map(Success(_): Try[_]).recover { case t => Failure(t) }) match {
      case Success(_) =>
        throw new AssertionError("No runtime exception was produced")
      case Failure(_: RuntimeException) =>
        succeed
      case Failure(t) =>
        throw t
    }
  }

  it should "succeed when each update is wrapped in an await " in async {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    await(service.createOrUpdate(DTO(1, "hello", t1)))
    await(service.createOrUpdate(DTO(1, "goodbye", t2)))

    repo.repo.get(1) match {
      case Some(dao) =>
        assert(dao.data == "goodbye")
        assert(dao.lastUpdated.contains(t2))
      case None =>
        throw new AssertionError("Failed to find record with id=1")
    }
  }
}
