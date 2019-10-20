package service

import java.util.Date

import dto.DTO
import model.DAObject
import repository.Repository

import scala.concurrent.{ ExecutionContext, Future }

class Service[K, V](val repo: Repository[K, V])(implicit ec: ExecutionContext) {

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
