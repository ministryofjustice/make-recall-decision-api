package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status

class HealthStatusAggregatorTest {
  @Test
  fun `given null statuses when called then return UP`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(null as MutableSet<Status>?)
    assertEquals(Status.UP, result)
  }

  @Test
  fun `given statuses containing UP when called then return UP`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(mutableSetOf(Status.UP))
    assertEquals(Status.UP, result)
  }

  @Test
  fun `given statuses containing UP and other statuses when called then return UP`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(mutableSetOf(Status.UP, Status("OTHER"), Status("ANOTHER")))
    assertEquals(Status.UP, result)
  }

  @Test
  fun `given statuses containing other statuses when called then return UP`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(mutableSetOf(Status("OTHER"), Status("ANOTHER")))
    assertEquals(Status.UP, result)
  }

  @Test
  fun `given statuses containing DOWN when called then return DOWN`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(mutableSetOf(Status.DOWN))
    assertEquals(Status.DOWN, result)
  }

  @Test
  fun `given statuses containing DOWN and other statuses when called then return DOWN`() {
    val aggregator = HealthStatusAggregator()
    val result = aggregator.getAggregateStatus(mutableSetOf(Status.DOWN, Status("OTHER"), Status("ANOTHER")))
    assertEquals(Status.DOWN, result)
  }
}
