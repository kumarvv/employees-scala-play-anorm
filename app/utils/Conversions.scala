package utils

import java.time.{ZoneId, Instant, LocalDateTime, LocalDate}
import java.util.{Date, Calendar}

object Conversions {

  implicit def any2str(av: Any): String =
    if (av != null) av.toString else ""

  implicit def any2double(av: Any): Double =
    if (av != null) av.asInstanceOf[Double] else 0

  implicit def any2long(av: Any): Long =
    if (av != null) av.asInstanceOf[Long] else 0

  implicit def any2bool(av: Any): Boolean =
    if (av != null) av.asInstanceOf[Boolean] else false

  implicit def any2date(av: Any): java.util.Date =
    if (av != null) av.asInstanceOf[java.util.Date] else null

  implicit def date2localdate(dv: java.util.Date): LocalDate = {
    val cal = Calendar.getInstance()
    cal.setTime(dv)
    if (dv != null) LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE)) else null
  }

  implicit def date2localdatetime(dv: java.util.Date): LocalDateTime =
    if (dv != null) LocalDateTime.ofInstant(dv.toInstant, java.time.ZoneId.systemDefault()) else null

  implicit def any2localdate(av: Any): LocalDate =
    if (av != null) date2localdate(av.asInstanceOf[java.util.Date]) else null

  implicit def localdate2date(lv: LocalDate): Date =
    if (lv != null) Date.from(lv.atStartOfDay(ZoneId.systemDefault()).toInstant()) else null
}
