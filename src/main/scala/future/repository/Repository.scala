package future.repository

import model.DAObject

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

/** In-memory future.repository for our DAO's.  Supports insert, update, and find. */
class Repository[K, T](implicit executionContext: ExecutionContext) {

  // This is public to simplify inspection in our tests.
  val repo: mutable.Map[K, DAObject[K, T]] = mutable.Map.empty[K, DAObject[K, T]]

  /**
   * Add the item to the repo unless its key is already in the repo in which case
   * we throw a runtime exception
   */
  def insert(data: DAObject[K, T]): Future[Unit] = {
    if (repo.contains(data.id)) {
      Future.failed(new RuntimeException(s"Duplicate key ${data.id}"))
    } else {
      Future.successful(repo.put(data.id, data))
    }
  }

  /**
   * Update the repo with the specified item unless it does not exist
   * in which case we throw a runtime exception.
   */
  def update(data: DAObject[K, T]): Future[Unit] = {
    repo.get(data.id) match {
      case Some(repoData) =>
        val updatedRepoData = repoData.copy(data = data.data, lastUpdated = data.lastUpdated)
        Future.successful(repo.put(repoData.id, updatedRepoData))
      case None =>
        Future.failed(new RuntimeException(s"Unknown key ${data.id}"))
    }
  }

  /** Return the specified item by its key or None if it does not exist. */
  def find(id: K): Future[Option[DAObject[K, T]]] = Future(repo.get(id))
}
