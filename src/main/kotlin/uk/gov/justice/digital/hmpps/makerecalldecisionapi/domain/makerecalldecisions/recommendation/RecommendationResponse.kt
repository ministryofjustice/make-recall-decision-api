package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.formatFullName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate
import java.time.LocalDateTime

data class RecommendationResponse(
  val userAccessResponse: UserAccess? = null,
  val id: Long? = null,
  val status: Status? = null,
  val custodyStatus: CustodyStatus? = null,
  val localPoliceContact: LocalPoliceContact? = null,
  val crn: String? = null,
  var sensitive: Boolean? = null,
  var reviewPractitionersConcerns: Boolean? = null,
  var reviewOffenderProfile: Boolean? = null,
  var explainTheDecision: Boolean? = null,
  var lastModifiedBy: String? = null,
  var lastModifiedByUserName: String? = null,
  var lastModifiedDate: String? = null,
  val managerRecallDecision: ManagerRecallDecision? = null,
  val recallType: RecallType? = null,
  val responseToProbation: String? = null,
  val triggerLeadingToRecall: String? = null,
  val spoRecallType: String? = null,
  val spoRecallRationale: String? = null,
  val whatLedToRecall: String? = null,
  val isThisAnEmergencyRecall: Boolean? = null,
  val isIndeterminateSentence: Boolean? = null,
  val isExtendedSentence: Boolean? = null,
  val activeCustodialConvictionCount: Number? = null,
  val hasVictimsInContactScheme: VictimsInContactScheme? = null,
  val indeterminateSentenceType: IndeterminateSentenceType? = null,
  val dateVloInformed: LocalDate? = null,
  val hasArrestIssues: SelectedWithDetails? = null,
  val hasContrabandRisk: SelectedWithDetails? = null,
  val personOnProbation: PersonOnProbationDto? = null,
  val alternativesToRecallTried: AlternativesToRecallTried? = null,
  val licenceConditionsBreached: LicenceConditionsBreached? = null,
  val cvlLicenceConditionsBreached: CvlLicenceConditionsBreached? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") val underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  val vulnerabilities: VulnerabilitiesRecommendation? = null,
  val convictionDetail: ConvictionDetail? = null,
  val region: String? = null,
  val localDeliveryUnit: String? = null,
  val userNameDntrLetterCompletedBy: String? = null,
  val lastDntrLetterDownloadDateTime: LocalDateTime? = null,
  val indexOffenceDetails: String? = null,
  val offenceDataFromLatestCompleteAssessment: Boolean? = null,
  val offencesMatch: Boolean? = null,
  val offenceAnalysis: String? = null,
  val fixedTermAdditionalLicenceConditions: SelectedWithDetails? = null,
  val indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails? = null,
  @JsonProperty("isMainAddressWherePersonCanBeFound") val mainAddressWherePersonCanBeFound: SelectedWithDetails? = null,
  val whyConsideredRecall: WhyConsideredRecall? = null,
  val reasonsForNoRecall: ReasonsForNoRecall? = null,
  val nextAppointment: NextAppointment? = null,
  val previousReleases: PreviousReleases? = null,
  val previousRecalls: PreviousRecalls? = null,
  val recallConsideredList: List<RecallConsidered>? = null,
  val currentRoshForPartA: RoshData? = null,
  val roshSummary: RoshSummary? = null,
  val countersignSpoTelephone: String? = null,
  val countersignSpoExposition: String? = null,
  val countersignAcoTelephone: String? = null,
  val countersignAcoExposition: String? = null
)

data class UnderIntegratedOffenderManagement(
  val selected: String? = null,
  val allOptions: List<TextValueOption>? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null,
  val middleNames: String? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val dateOfBirth: LocalDate? = null,
  val croNumber: String? = null,
  val mostRecentPrisonerNumber: String? = null,
  val nomsNumber: String? = null,
  val pncNumber: String? = null,
  var mappa: Mappa? = null,
  val addresses: List<Address>? = null,
  val primaryLanguage: String? = null,
  val hasBeenReviewed: Boolean? = false
)

fun PersonOnProbation.toPersonOnProbationDto(): PersonOnProbationDto {
  val firstName = this.firstName
  val middleNames = this.middleNames
  val surname = this.surname
  return PersonOnProbationDto(
    fullName = formatFullName(firstName, middleNames, surname),
    name = this.name,
    firstName = firstName,
    surname = surname,
    middleNames = middleNames,
    gender = this.gender,
    ethnicity = this.ethnicity,
    dateOfBirth = this.dateOfBirth,
    croNumber = this.croNumber,
    mostRecentPrisonerNumber = this.mostRecentPrisonerNumber,
    nomsNumber = this.nomsNumber,
    pncNumber = this.pncNumber,
    mappa = this.mappa,
    addresses = this.addresses,
    primaryLanguage = this.primaryLanguage,
    hasBeenReviewed = this.hasBeenReviewed
  )
}

data class PersonOnProbationDto(
  val fullName: String?,
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null,
  val middleNames: String? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val dateOfBirth: LocalDate? = null,
  val croNumber: String? = null,
  val mostRecentPrisonerNumber: String? = null,
  val nomsNumber: String? = null,
  val pncNumber: String? = null,
  var mappa: Mappa? = null,
  val addresses: List<Address>? = null,
  val primaryLanguage: String? = null,
  val hasBeenReviewed: Boolean? = false
)

data class ConvictionDetail(
  val indexOffenceDescription: String? = null,
  val dateOfOriginalOffence: LocalDate? = null,
  val dateOfSentence: LocalDate? = null,
  val lengthOfSentence: Int? = null,
  val lengthOfSentenceUnits: String? = null,
  val sentenceDescription: String? = null,
  val licenceExpiryDate: LocalDate? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val sentenceSecondLength: Int? = null,
  val sentenceSecondLengthUnits: String? = null,
  val custodialTerm: String? = null,
  val extendedTerm: String? = null,
  val hasBeenReviewed: Boolean? = false
)
