package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

data class UpdateRecommendationRequest(
  val recallType: RecallType?,
  val status: Status?
)
