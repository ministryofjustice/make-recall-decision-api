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

fun randomDoubleOrNull(nullChance: Double = 0.5): Double? = if (Random.Default.nextDouble() < nullChance) {
  null
} else {
  Random.Default.nextDouble()
}
fun randomBoolean(): Boolean = Random.Default.nextBoolean()

/**
 * Produces a random date between 1970-01-01 and 2150-12-31
 */
fun randomLocalDate(): LocalDate {
  val minDay = 0L
  val maxDay = LocalDate.of(2150, 12, 31).toEpochDay()
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
