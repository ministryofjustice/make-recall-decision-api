package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class ActiveRecommendation(
  val recommendationId: Long?,
  val lastModifiedDate: String?,
  val lastModifiedBy: String?
)
