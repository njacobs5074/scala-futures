package dto

import java.util.Date

/** Data Transfer Object -- Simulates passing in external data to our repo. */
case class DTO[K, V](id: K, data: V, lastUpdated: Date)
