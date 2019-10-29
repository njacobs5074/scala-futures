package model

import java.util.Date

/** This class models how we store our data in a persistent storage layer. */
case class DAObject[K, T](data: T, id: K, created: Date, lastUpdated: Option[Date] = None)
