package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PartAData(
  val custodyStatus: String,
  val recallType: RecallTypePartA,
  val responseToProbation: String?,
  val isThisAnEmergencyRecall: String?,
)

data class RecallTypePartA(
  val value: String?,
  val details: String?
)
