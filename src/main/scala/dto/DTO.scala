package dto

import java.util.Date

case class DTO[K, V](id: K, data: V, lastUpdated: Date)
