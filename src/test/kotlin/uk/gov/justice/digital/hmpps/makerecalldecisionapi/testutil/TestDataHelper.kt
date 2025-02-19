package uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.random.Random

fun randomString(): String {
  return UUID.randomUUID().toString()
}

fun randomInt(): Int {
  return Random.Default.nextInt()
}

fun randomLong(): Long {
  return Random.Default.nextLong()
}

fun randomDouble(): Double {
  return Random.Default.nextDouble()
}

fun randomBoolean(): Boolean {
  return Random.Default.nextBoolean()
}

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
 * Produces a random date and time between 1970-01-01T00:00:00 and 2150-12-31T23:59:59
 */
fun randomLocalDateTime(): LocalDateTime {
  val minSecond = 0L
  val maxSecond = LocalDateTime.of(2150, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC)
  val randomSecond: Long = Random.Default.nextLong(minSecond, maxSecond)
  return LocalDateTime.ofEpochSecond(randomSecond, 0, ZoneOffset.UTC)
}

inline fun <reified E : Enum<E>> randomEnum(): E {
  return enumValues<E>()[Random.Default.nextInt(0, enumValues<E>().size)]
}
