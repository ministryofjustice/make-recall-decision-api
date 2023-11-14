package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.boot.actuate.health.Status
import org.springframework.boot.actuate.health.StatusAggregator
import org.springframework.stereotype.Component

@Component
class HealthStatusAggregator : StatusAggregator {
  override fun getAggregateStatus(statuses: MutableSet<Status>?): Status {
    return if (statuses != null && statuses.any { status -> status == Status.DOWN }) Status.DOWN else Status.UP
  }
}
