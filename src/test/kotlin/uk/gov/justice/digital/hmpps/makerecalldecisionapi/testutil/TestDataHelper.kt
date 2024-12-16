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

fun randomLocalDate(): LocalDate {
  val minDay = 0L
  val maxDay = LocalDate.of(2150, 12, 31).toEpochDay()
  val randomDay: Long = Random.Default.nextLong(minDay, maxDay)
  return LocalDate.ofEpochDay(randomDay)
}

fun randomLocalDateTime(): LocalDateTime {
  val minSecond = 0L
  val maxSecond = LocalDateTime.of(2150, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC)
  val randomSecond: Long = Random.Default.nextLong(minSecond, maxSecond)
  return LocalDateTime.ofEpochSecond(randomSecond, 0, ZoneOffset.UTC)
}