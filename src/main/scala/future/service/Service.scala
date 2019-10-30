package future.service

import java.util.Date

import dto.DTO
import model.DAObject
import future.repository.Repository

import scala.concurrent.{ ExecutionContext, Future }

/** Class to simulate a future.service layer object that uses the Repository for persistence. */
class Service[K, V](val repo: Repository[K, V])(implicit ec: ExecutionContext) {

  /**
   * Create or update the specified DTO in the repo if it is newer than what we have.
   * We quietly ignore stale updates.
   */
  def createOrUpdate(dto: DTO[K, V]): Future[Unit] = {
    repo.find(dto.id).flatMap {
      case Some(dao) if dao.lastUpdated.isEmpty || dao.lastUpdated.exists(_.before(dto.lastUpdated)) =>
        repo.update(dao.copy(data = dto.data, lastUpdated = Some(dto.lastUpdated)))
      case Some(_) => // Disregard old updates
        Future.successful(())
      case None =>
        repo.insert(DAObject(dto.data, dto.id, new Date()))
    }
  }

}
