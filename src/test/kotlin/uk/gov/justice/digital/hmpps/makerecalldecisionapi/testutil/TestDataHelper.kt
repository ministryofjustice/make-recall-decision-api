package uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

fun randomString(): String = UUID.randomUUID().toString()

fun randomInt(): Int = Random.Default.nextInt()

fun randomLong(): Long = Random.Default.nextLong()

fun randomDouble(): Double = Random.Default.nextDouble()

fun randomBoolean(): Boolean = Random.Default.nextBoolean()

fun randomPastLocalDate(): LocalDate = randomLocalDate(maxDate = LocalDate.now())

fun randomFutureLocalDate(): LocalDate = randomLocalDate(minDate = LocalDate.now())

/**
 * Produces a random date between 1970-01-01 and 2150-12-31, or within the min and max if provided
 */
fun randomLocalDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate {
  val minDay = minDate?.toEpochDay() ?: 0L
  val maxDay = (maxDate ?: LocalDate.of(2150, 12, 31)).toEpochDay()
  val randomDay: Long = Random.Default.nextLong(minDay, maxDay)
  return LocalDate.ofEpochDay(randomDay)
}

/**
 * Produces a random time between 00:00:00:000000000 and 23:59:59:999999999
 */
fun randomLocalTime(): LocalTime = LocalTime.of(
  Random.nextInt(0, 23),
  Random.nextInt(0, 59),
  Random.nextInt(0, 59),
  Random.nextInt(0, 999999999),
)

/**
 * Produces a random date and time between 1970-01-01T00:00:00 and 2150-12-31T23:59:59
 */
fun randomLocalDateTime(): LocalDateTime {
  val minSecond = 0L
  val maxSecond = LocalDateTime.of(2150, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC)
  val randomSecond: Long = Random.Default.nextLong(minSecond, maxSecond)
  return LocalDateTime.ofEpochSecond(randomSecond, 0, ZoneOffset.UTC)
}

/**
 * Produces a random date and time between 1970-01-01T00:00:00 and 2150-12-31T23:59:59
 */
fun randomZonedDateTime(): ZonedDateTime = ZonedDateTime.ofLocal(randomLocalDateTime(), ZoneId.of("UTC"), ZoneOffset.UTC)

inline fun <reified E : Enum<E>> randomEnum(): E = enumValues<E>()[Random.Default.nextInt(0, enumValues<E>().size)]
