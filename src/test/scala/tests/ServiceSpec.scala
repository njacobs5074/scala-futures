package tests

import java.util.Date

import dto.DTO
import org.scalatest.AsyncFlatSpec
import repository.Repository
import service.Service

import scala.concurrent.Future

/** Tests to exercise the Service with a single threaded executor */
class ServiceSpec extends AsyncFlatSpec {

  "Service" should "add new item" in {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val t1 = new Date()
    val f = service.createOrUpdate(DTO(1, "hello", t1))
    f.map { _ =>
      assert(repo.repo.get(1).exists(dao => dao.id == 1 && dao.data == "hello" && dao.lastUpdated.isEmpty))
    }
  }

  it should "fail if updates run in parallel" in {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    val f1 = service.createOrUpdate(DTO(1, "hello", t1))
    val f2 = service.createOrUpdate(DTO(1, "goodbye", t2))

    recoverToSucceededIf[RuntimeException](Future.sequence(List(f1, f2)))
  }

  it should "fail second update when futures are vanilla mapped" in {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    val f1 = service.createOrUpdate(DTO(1, "hello", t1))
    val f2 = service.createOrUpdate(DTO(1, "goodbye", t2))

    f1.map(_ => f2).map { _ =>
      repo.repo.get(1) match {
        case Some(dao) =>
          assert(dao.data == "hello")
          assert(dao.lastUpdated.isEmpty)
        case None =>
          throw new AssertionError("Failed to find record with id=1")
      }
    }
  }

  it should "correctly serialize with a for comprehension" in {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    for {
      _ <- service.createOrUpdate(DTO(1, "hello", t1))
      _ <- service.createOrUpdate(DTO(1, "goodbye", t2))
    } yield {
      repo.repo.get(1) match {
        case Some(dao) =>
          assert(dao.data == "goodbye")
          assert(dao.lastUpdated.contains(t2))
        case None =>
          throw new AssertionError("Failed to find record with id=1")
      }
    }
  }

  it should "correctly serialize when using flatMap" in {
    val repo = new Repository[Int, String]()(newRepoExecutionContext)
    val service = new Service[Int, String](repo)(newServiceExecutionContext)

    val (t1, t2) = getSuccessiveTimestamps

    val future = service.createOrUpdate(DTO(1, "hello", t1)).flatMap { _ =>
      service.createOrUpdate(DTO(1, "goodbye", t2))
    }

    future.map { _ =>
      repo.repo.get(1) match {
        case Some(dao) =>
          assert(dao.data == "goodbye")
          assert(dao.lastUpdated.contains(t2))
        case None =>
          throw new AssertionError("Failed to find record with id=1")
      }
    }
  }
}
