import java.util.Date
import java.util.concurrent.Executors

import model._
import org.scalatest.Matchers._
import org.scalatest._
import repository.Repository

import scala.concurrent.ExecutionContext

class RepositorySpec extends AsyncFlatSpec {

  "Repository" should "insert DAO" in {

    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5)))

    val f = repo.insert(DAObject[Int, String](id = 1, data = "Hello", created = new Date()))
    f.map { _ =>
      assert(repo.repo.get(1).exists(_.data == "Hello"))
    }
  }

  it should "update a DAO" in {
    val repo = new Repository[Int, String]()(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5)))

    val dao = new DAObject[Int, String](id = 1, data = "Hello", created = new Date())
    val f1 = repo.insert(dao)
    val f2 = f1.flatMap(_ => repo.update(dao.copy(data = "Goodbye", lastUpdated = Some(new Date()))))

    f2.map { _ =>
      assert(repo.repo.get(1).exists(_.data == "Goodbye"))
      assert(repo.repo.get(1).exists(_.lastUpdated.nonEmpty))
    }
  }
}
