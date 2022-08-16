package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PartAData(
  val custodyStatus: String,
  val recallType: ValueWithDetails,
  val responseToProbation: String?,
  val isThisAnEmergencyRecall: String?,
  val hasVictimsInContactScheme: String?,
  val dateVloInformed: String?,
  val selectedAlternativesMap: Map<String, String>,
  val hasArrestIssues: ValueWithDetails?,
)

data class ValueWithDetails(
  val value: String?,
  val details: String?
)
