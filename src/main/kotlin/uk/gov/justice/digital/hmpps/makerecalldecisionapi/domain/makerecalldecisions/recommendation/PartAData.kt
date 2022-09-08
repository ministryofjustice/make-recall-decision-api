package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate

data class PartAData(
  val custodyStatus: ValueWithDetails? = null,
  val recallType: ValueWithDetails? = null,
  val responseToProbation: String? = null,
  val whatLedToRecall: String? = null,
  val isThisAnEmergencyRecall: String? = null,
  val hasVictimsInContactScheme: String? = null,
  val dateVloInformed: String? = null,
  val selectedAlternatives: List<ValueWithDetails>? = null,
  val hasArrestIssues: ValueWithDetails? = null,
  val hasContrabandRisk: ValueWithDetails? = null,
  val selectedStandardConditionsBreached: List<String>? = null,
  val additionalConditionsBreached: String? = null,
  val isUnderIntegratedOffenderManagement: String? = null,
  val localPoliceContact: LocalPoliceContact? = null,
  val vulnerabilities: Vulnerabilities? = null,
  val gender: String? = null,
  val dateOfBirth: LocalDate? = null,
  val name: String? = null,
  val ethnicity: String? = null,
  val croNumber: String? = null,
  val pncNumber: String? = null,
  val mostRecentPrisonerNumber: String? = null,
  val nomsNumber: String? = null,
  val indexOffenceDescription: String? = null,
  val dateOfOriginalOffence: String? = null,
  val dateOfSentence: String? = null,
  val lengthOfSentence: String? = null,
  val licenceExpiryDate: String? = null,
  val sentenceExpiryDate: String? = null,
  val custodialTerm: String? = null,
  val extendedTerm: String? = null,
  val mappaLevel: String? = null,
  val mappaCategory: String? = null
)

data class ValueWithDetails(
  val value: String? = null,
  val details: String? = null
)
