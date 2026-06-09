package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.boot.health.actuate.endpoint.StatusAggregator
import org.springframework.boot.health.contributor.Status
import org.springframework.stereotype.Component

@Component
class HealthStatusAggregator : StatusAggregator {
  override fun getAggregateStatus(statuses: MutableSet<Status>): Status = if (statuses.any { status -> status == Status.DOWN }) Status.DOWN else Status.UP
}
