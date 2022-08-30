package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PartAData(
  val custodyStatus: String? = null,
  val recallType: ValueWithDetails? = null,
  val responseToProbation: String? = null,
  val isThisAnEmergencyRecall: String? = null,
  val hasVictimsInContactScheme: String? = null,
  val dateVloInformed: String? = null,
  val selectedAlternatives: List<ValueWithDetails>? = null,
  val hasArrestIssues: ValueWithDetails? = null,
  val selectedStandardConditionsBreached: List<String>? = null,
  val additionalConditionsBreached: String? = null,
  val isUnderIntegratedOffenderManagement: String? = null,
  val localPoliceContact: LocalPoliceContact? = null
)

data class ValueWithDetails(
  val value: String? = null,
  val details: String? = null
)
