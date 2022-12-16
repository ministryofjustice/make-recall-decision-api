package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureFlags(
  val flagSendDomainEvent: Boolean? = false,
  val flagDomainEventConsiderRecall: Boolean? = false,
  val flagDomainEventRecommendationStarted: Boolean? = false
)
