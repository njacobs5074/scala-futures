import java.util.Calendar

package object tests {

  /** Get 2 dates that are 1 second apart. */
  def getSuccessiveTimestamps = {
    val c = Calendar.getInstance()
    val t1 = c.getTime
    c.add(Calendar.SECOND, 1)
    val t2 = c.getTime
    (t1, t2)
  }
}
