package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class ReasonsForNoRecall(
  val licenceBreach: String? = null,
  val noRecallRationale: String? = null,
  val popProgressMade: String? = null,
  val futureExpectations: String? = null,
)
