package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

data class RecommendationResponse(
  val id: Long? = null,
  val status: Status? = null,
  val crn: String? = null,
  val recallType: RecallType? = null
)
