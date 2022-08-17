package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PartAData(
  val custodyStatus: String? = null,
  val recallType: ValueWithDetails? = null,
  val responseToProbation: String? = null,
  val isThisAnEmergencyRecall: String? = null,
  val hasVictimsInContactScheme: String? = null,
  val dateVloInformed: String? = null,
  val selectedAlternativesMap: Map<String, String> = emptyMap(),
  val hasArrestIssues: ValueWithDetails? = null,
)

data class ValueWithDetails(
  val value: String? = null,
  val details: String? = null
)
