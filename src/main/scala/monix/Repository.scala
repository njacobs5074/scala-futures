package monix

import model.DAObject
import monix.eval.Task
import monix.execution.Scheduler

import scala.collection.mutable

/** In-memory future.repository for our DAO's.  Supports insert, update, and find. */
class Repository[K, T](scheduler: Scheduler) {

  // This is public to simplify inspection in our tests.
  val repo: mutable.Map[K, DAObject[K, T]] = mutable.Map.empty[K, DAObject[K, T]]

  /**
   * Add the item to the repo unless its key is already in the repo in which case
   * we throw a runtime exception
   */
  def insert(data: DAObject[K, T]): Task[Option[DAObject[K, T]]] = {
    if (repo.contains(data.id)) {
      Task.raiseError(new RuntimeException(s"Duplicate key ${data.id}"))
    } else {
      Task(repo.put(data.id, data))
    }
  }.executeOn(scheduler)

  /**
   * Update the repo with the specified item unless it does not exist
   * in which case we throw a runtime exception.
   */
  def update(data: DAObject[K, T]): Task[Option[DAObject[K, T]]] = {
    repo.get(data.id) match {
      case Some(repoData) =>
        val updatedRepoData = repoData.copy(data = data.data, lastUpdated = data.lastUpdated)
        Task(repo.put(repoData.id, updatedRepoData))
      case None =>
        Task.raiseError(new RuntimeException(s"Unknown key ${data.id}"))
    }
  }.executeOn(scheduler)

  /** Return the specified item by its key or None if it does not exist. */
  def find(id: K): Task[Option[DAObject[K, T]]] = Task(repo.get(id))
}
