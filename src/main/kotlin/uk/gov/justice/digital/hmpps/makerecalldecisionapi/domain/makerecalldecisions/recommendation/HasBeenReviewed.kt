package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class HasBeenReviewed(
  val personOnProbation: Boolean = false,
  val convictionDetail: Boolean = false,
  val mappa: Boolean = false,
)
