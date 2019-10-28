package repository

import model.DAObject

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

class Repository[K, T](implicit executionContext: ExecutionContext) {
  val repo: mutable.Map[K, DAObject[K, T]] = mutable.Map.empty[K, DAObject[K, T]]

  def insert(data: DAObject[K, T]): Future[Unit] = Future {
    if (repo.contains(data.id)) {
      throw new RuntimeException(s"Duplicate key ${data.id}")
    } else {
      repo.put(data.id, data)
    }
  }

  def update(data: DAObject[K, T]): Future[Unit] = Future {
    repo.get(data.id) match {
      case Some(repoData) =>
        val updatedRepoData = repoData.copy(data = data.data, lastUpdated = data.lastUpdated)
        repo.put(repoData.id, updatedRepoData)
      case None =>
        throw new RuntimeException(s"Unknown key ${data.id}")
    }
  }

  def find(id: K): Future[Option[DAObject[K, T]]] = Future[Option[DAObject[K, T]]] {
    repo.get(id)
  }
}
