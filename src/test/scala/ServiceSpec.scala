import java.util.concurrent.Executors
import java.util.{ Calendar, Date }

import dto.DTO
import org.scalatest.AsyncFlatSpec
import repository.Repository
import service.Service

import scala.concurrent.ExecutionContext

class ServiceSpec extends AsyncFlatSpec {
  "Service" should "add new item" in {
    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5)))
    val service = new Service[Int, String](repo)(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))

    val t1 = new Date()
    val f = service.createOrUpdate(DTO(1, "hello", t1))
    f.map { _ =>
      assert(repo.repo.get(1).exists(dao => dao.id == 1 && dao.data == "hello" && dao.lastUpdated.isEmpty))
    }
  }

  it should "not allow the item with the same key to be added twice" in {
    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
    val service = new Service[Int, String](repo)(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))

    val (t1, t2) = {
      val c = Calendar.getInstance()
      val t1 = c.getTime
      c.add(Calendar.SECOND, 1)
      val t2 = c.getTime
      (t1, t2)
    }

    // If we don't flatMap the first call's future, they run in parallel.  Even with a single threaded executor.
    val f = service.createOrUpdate(DTO(1, "hello", t1)).flatMap { _ =>
      service.createOrUpdate(DTO(1, "goodbye", t2))
    }

    f.map { _ =>
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
