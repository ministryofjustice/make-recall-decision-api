package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class HasBeenReviewed(
  val personOnProbation: Boolean = false,
)
