package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.JsonProperty
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.BookRecallToPpud
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.BookingMemento
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConsiderationRationale
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HasBeenReviewed
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NomisIndexOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerForPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PrisonOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilitiesRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhoCompletedPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toPersonOnProbationDto
import java.io.Serializable
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.abs

@Entity
@Table(name = "recommendations")
data class RecommendationEntity(
  @Id
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb")
  var data: RecommendationModel,
  var deleted: Boolean = false,
) : Comparable<RecommendationEntity> {
  override fun compareTo(other: RecommendationEntity) = compareValuesBy(
    other,
    this,
  ) { it.data.lastModifiedDate }
}

fun RecommendationEntity.toRecommendationResponse(): RecommendationResponse = RecommendationResponse(
  id = id,
  status = data.status,
  personOnProbation = data.personOnProbation?.toPersonOnProbationDto(),
)

data class RecommendationModel(
  val crn: String?,
  var sensitive: Boolean? = null,
  var ppudRecordPresent: Boolean = false,
  var recallConsideredList: List<RecallConsidered>? = null,
  var recallType: RecallType? = null,
//  @JsonIgnore
  val sendSpoRationaleToDelius: Boolean? = false,
  @JsonMerge
  var managerRecallDecision: ManagerRecallDecision? = null,
  var considerationRationale: ConsiderationRationale? = null,
  var custodyStatus: CustodyStatus? = null,
  var localPoliceContact: LocalPoliceContact? = null,
  var responseToProbation: String? = null,
  // deprecated
  var thoughtsLeadingToRecall: String? = null,
  var triggerLeadingToRecall: String? = null,
  var whatLedToRecall: String? = null,
  @JsonProperty("isThisAnEmergencyRecall") var isThisAnEmergencyRecall: Boolean? = null,
  @JsonProperty("isIndeterminateSentence") var isIndeterminateSentence: Boolean? = null,
  @JsonProperty("isExtendedSentence") var isExtendedSentence: Boolean? = null,
  var activeCustodialConvictionCount: Number? = null,
  var hasVictimsInContactScheme: VictimsInContactScheme? = null,
  var indeterminateSentenceType: IndeterminateSentenceType? = null,
  @JsonFormat(pattern = "yyyy-MM-dd") var dateVloInformed: LocalDate? = null,
  var hasArrestIssues: SelectedWithDetails? = null,
  var hasContrabandRisk: SelectedWithDetails? = null,
  var status: Status? = null,
  var region: String? = null,
  var localDeliveryUnit: String? = null,
  var userNameDntrLetterCompletedBy: String? = null,
  var lastDntrLetterADownloadDateTime: LocalDateTime? = null,
  var reviewPractitionersConcerns: Boolean? = null,
  var odmName: String? = null,
  var spoRecallType: String? = null,
  var spoRecallRationale: String? = null,
  var spoDeleteRecommendationRationale: String? = null,
  var sendSpoDeleteRationaleToDelius: Boolean? = false,
  // deprecated
  var spoCancelRecommendationRationale: String? = null,
  var reviewOffenderProfile: Boolean? = null,
  var explainTheDecision: Boolean? = null,
  var lastModifiedBy: String? = null,
  var lastModifiedByUserName: String? = null,
  var lastModifiedDate: String? = null,
  val createdBy: String? = null,
  val createdByUserFullName: String? = null,
  val createdDate: String? = null,
  var personOnProbation: PersonOnProbation? = null,
  var convictionDetail: ConvictionDetail? = null,
  var alternativesToRecallTried: AlternativesToRecallTried? = null,
  var licenceConditionsBreached: LicenceConditionsBreached? = null,
  var cvlLicenceConditionsBreached: CvlLicenceConditionsBreached? = null,
  val additionalLicenceConditionsText: String? = null,
  var vulnerabilities: VulnerabilitiesRecommendation? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") var underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  var indexOffenceDetails: String? = null,
  var offenceDataFromLatestCompleteAssessment: Boolean? = null,
  var offencesMatch: Boolean? = null,
  var offenceAnalysis: String? = null,
  var fixedTermAdditionalLicenceConditions: SelectedWithDetails? = null,
  var indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails? = null,
  @JsonProperty("isMainAddressWherePersonCanBeFound") var mainAddressWherePersonCanBeFound: SelectedWithDetails? = null,
  var whyConsideredRecall: WhyConsideredRecall? = null,
  var reasonsForNoRecall: ReasonsForNoRecall? = null,
  var nextAppointment: NextAppointment? = null,
  var hasBeenReviewed: HasBeenReviewed? = null,
  @JsonMerge
  var previousReleases: PreviousReleases? = null,
  @JsonMerge
  var previousRecalls: PreviousRecalls? = null,
  var recommendationStartedDomainEventSent: Boolean? = null,
  var currentRoshForPartA: RoshData? = null,
  var roshSummary: RoshSummary? = null,
  val countersignSpoTelephone: String? = null,
  val countersignSpoExposition: String? = null,
  val countersignAcoExposition: String? = null,
  val countersignAcoTelephone: String? = null,
  var whoCompletedPartA: WhoCompletedPartA? = null,
  var practitionerForPartA: PractitionerForPartA? = null,
  var revocationOrderRecipients: List<String>? = null,
  var decisionDateTime: LocalDateTime? = null,
  var ppcsQueryEmails: List<String>? = null,
  var prisonOffender: PrisonOffender? = null,
  var prisonApiLocationDescription: String? = null,
  val releaseUnderECSL: Boolean? = null,
  val dateOfRelease: LocalDate? = null,
  val conditionalReleaseDate: LocalDate? = null,
  val nomisIndexOffence: NomisIndexOffence? = null,
  val bookRecallToPpud: BookRecallToPpud? = null,
  val ppudOffender: PpudOffender? = null,
  val bookingMemento: BookingMemento? = null,
  // deprecated
  var isOver18: Boolean? = null,
  var isUnder18: Boolean? = null,
  var isMappaLevelAbove1: Boolean? = null,
  // deprecated
  var isSentenceUnder12Months: Boolean? = null,
  var isSentence12MonthsOrOver: Boolean? = null,
  var hasBeenConvictedOfSeriousOffence: Boolean? = null,
  // deprecated
  var userNamePartACompletedBy: String? = null,
  // deprecated
  var userEmailPartACompletedBy: String? = null,
  // deprecated
  var lastPartADownloadDateTime: LocalDateTime? = null,
  // deprecated
  var countersignSpoDateTime: LocalDateTime? = null,
  // deprecated
  var countersignSpoName: String? = null,
  // deprecated
  var acoCounterSignEmail: String? = null,
  // deprecated
  var spoCounterSignEmail: String? = null,
  // deprecated
  var countersignAcoName: String? = null,
  // deprecated
  var countersignAcoDateTime: LocalDateTime? = null,
  // deprecated
  var deleted: Boolean = false,
) : Serializable

enum class Status {
  DRAFT,
  DELETED,
  RECALL_CONSIDERED,
  DOCUMENT_DOWNLOADED,
  DOCUMENT_CREATED,
}

data class TextValueOption(
  val value: String? = null,
  val text: String? = null,
)
