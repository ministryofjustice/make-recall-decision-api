package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel

data class RecommendationHistoryResponse(
  val recommendationId: Long?,
  val crn: String,
  val recommendations: List<RecommendationModel>? = null,
)
