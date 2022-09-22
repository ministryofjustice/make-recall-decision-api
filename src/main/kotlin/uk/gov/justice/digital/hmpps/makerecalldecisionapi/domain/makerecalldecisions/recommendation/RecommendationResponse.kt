package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate
import java.time.LocalDateTime

data class RecommendationResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val id: Long? = null,
  val status: Status? = null,
  val custodyStatus: CustodyStatus? = null,
  val localPoliceContact: LocalPoliceContact? = null,
  val crn: String? = null,
  val recallType: RecallType? = null,
  val responseToProbation: String? = null,
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
  val personOnProbation: PersonOnProbation? = null,
  val alternativesToRecallTried: AlternativesToRecallTried? = null,
  val licenceConditionsBreached: LicenceConditionsBreached? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") val underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  val vulnerabilities: Vulnerabilities? = null,
  val convictionDetail: ConvictionDetail? = null,
  val region: String? = null,
  val localDeliveryUnit: String? = null,
  val userNamePartACompletedBy: String? = null,
  val userEmailPartACompletedBy: String? = null,
  val lastPartADownloadDateTime: LocalDateTime? = null,
  val indexOffenceDetails: String? = null,
  val fixedTermAdditionalLicenceConditions: SelectedWithDetails? = null,
  val indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails? = null,
  @JsonProperty("isMainAddressWherePersonCanBeFound") val mainAddressWherePersonCanBeFound: SelectedWithDetails? = null,
  val whyConsideredRecall: WhyConsideredRecall? = null
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
  val mappa: Mappa? = null,
  val addresses: List<Address>? = null
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
  val sentenceSecondLengthUnits: String? = null
)
