package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import java.time.LocalDate

data class DocumentData(
  val salutation: String? = null,
  val letterTitle: String? = null,
  val letterDate: String? = null,
  val section1: String? = null,
  val section2: String? = null,
  val section3: String? = null,
  val signedByParagraph: String? = null,
  val custodyStatus: ValueWithDetails? = null,
  val recallType: ValueWithDetails? = null,
  val responseToProbation: String? = null,
  val whatLedToRecall: String? = null,
  val isThisAnEmergencyRecall: String? = null,
  val isExtendedSentence: String? = null,
  val hasVictimsInContactScheme: String? = null,
  val indeterminateSentenceType: String? = null,
  val dateVloInformed: String? = null,
  val selectedAlternatives: List<ValueWithDetails>? = null,
  val hasArrestIssues: ValueWithDetails? = null,
  val hasContrabandRisk: ValueWithDetails? = null,
  val selectedStandardConditionsBreached: List<String>? = null,
  val additionalConditionsBreached: String? = null,
  val isUnderIntegratedOffenderManagement: String? = null,
  val localPoliceContact: LocalPoliceContact? = null,
  val vulnerabilities: VulnerabilitiesRecommendation? = null,
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
  val mappa: Mappa? = null,
  val lastRecordedAddress: String? = null,
  val letterAddress: String? = null,
  val noFixedAbode: String? = null,
  val lastPersonCompletingFormName: String? = null,
  val lastPersonCompletingFormEmail: String? = null,
  val region: String? = null,
  val localDeliveryUnit: String? = null,
  var dateOfDecision: String? = null,
  var timeOfDecision: String? = null,
  val offenceAnalysis: String? = null,
  val fixedTermAdditionalLicenceConditions: String? = null,
  val behaviourSimilarToIndexOffence: String? = null,
  val behaviourSimilarToIndexOffencePresent: String? = null,
  val behaviourLeadingToSexualOrViolentOffence: String? = null,
  val behaviourLeadingToSexualOrViolentOffencePresent: String? = null,
  val outOfTouch: String? = null,
  val outOfTouchPresent: String? = null,
  val otherPossibleAddresses: String? = null,
  val primaryLanguage: String? = null,
  val lastReleasingPrison: String? = null,
  val lastReleaseDate: String? = null,
  val datesOfLastReleases: String? = null,
  val datesOfLastRecalls: String? = null,
  val riskToChildren: String? = null,
  val riskToPublic: String? = null,
  val riskToKnownAdult: String? = null,
  val riskToStaff: String? = null,
  val riskToPrisoners: String? = null
)

data class ValueWithDetails(
  val value: String? = null,
  val details: String? = null
)
