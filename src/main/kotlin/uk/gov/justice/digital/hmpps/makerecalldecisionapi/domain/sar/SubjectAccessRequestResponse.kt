package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.sar

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel

data class SubjectAccessRequestResponse(
  val crn: String,
  val recommendations: List<RecommendationModel>,
)
