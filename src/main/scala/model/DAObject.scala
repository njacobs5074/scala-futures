package model

import java.util.Date

case class DAObject[K, T](data: T, id: K, created: Date, lastUpdated: Option[Date] = None)
