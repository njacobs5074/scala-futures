package tests.monix

import java.util.Date

import tests.getSuccessiveTimestamps
import tests.{repoScheduler, serviceScheduler}
import dto.DTO
import monix.Repository
import monix.Service
import monix.eval.Task
import org.scalatest.AsyncFlatSpec
import tests.{newRepoExecutionContext, newServiceExecutionContext}
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

class MonixServiceSpec extends AsyncFlatSpec {

  "Service" should "add new item" in {

    val repo = new Repository[Int, String](repoScheduler)
    val service = new Service[Int, String](repo)

    val t1 = new Date()
    val f = service.createOrUpdate(DTO(1, "hello", t1))
    f.map { _ =>
      assert(repo.repo.get(1).exists(dao => dao.id == 1 && dao.data == "hello" && dao.lastUpdated.isEmpty))
    }.runToFuture(serviceScheduler)
  }

  it should "not fail if updates run in parallel" in {

    val repo = new Repository[Int, String](repoScheduler)
    val service = new Service[Int, String](repo)

    val (t1, t2) = getSuccessiveTimestamps

    val f1 = service.createOrUpdate(DTO(1, "hello", t1))
    val f2 = service.createOrUpdate(DTO(1, "goodbye", t2))

    assert(Await.result(Task.sequence(List(f1, f2)).runToFuture(serviceScheduler), 10 seconds).size == 2)
  }

  it should "fail second update when futures are vanilla mapped" in {
    val repo = new Repository[Int, String](repoScheduler)
    val service = new Service[Int, String](repo)

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
    }.runToFuture(serviceScheduler)
  }

  it should "correctly serialize with a for comprehension" in {
    val repo = new Repository[Int, String](repoScheduler)
    val service = new Service[Int, String](repo)

    val (t1, t2) = getSuccessiveTimestamps

    (for {
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
    }).runToFuture(serviceScheduler)
  }

  it should "correctly serialize when using flatMap" in {
    val repo = new Repository[Int, String](repoScheduler)
    val service = new Service[Int, String](repo)

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
    }.runToFuture(serviceScheduler)
  }

}
