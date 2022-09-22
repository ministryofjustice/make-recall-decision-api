package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.Vulnerabilities
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import java.io.Serializable
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recommendations")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RecommendationEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  var data: RecommendationModel
) : Comparable<RecommendationEntity> {
  override fun compareTo(other: RecommendationEntity) = compareValuesBy(
    other, this
  ) { it.data.lastModifiedDate }
}

data class RecommendationModel(
  val crn: String?,
  var recallType: RecallType? = null,
  var custodyStatus: CustodyStatus? = null,
  var localPoliceContact: LocalPoliceContact? = null,
  var responseToProbation: String? = null,
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
  var userNamePartACompletedBy: String? = null,
  var userEmailPartACompletedBy: String? = null,
  var lastPartADownloadDateTime: LocalDateTime? = null,
  var userNameDntrLetterCompletedBy: String? = null,
  var lastDntrLetterADownloadDateTime: LocalDateTime? = null,
  var lastModifiedBy: String? = null,
  var lastModifiedDate: String? = null,
  val createdBy: String? = null,
  val createdDate: String? = null,
  val personOnProbation: PersonOnProbation? = null,
  val convictionDetail: ConvictionDetail? = null,
  var alternativesToRecallTried: AlternativesToRecallTried? = null,
  var licenceConditionsBreached: LicenceConditionsBreached? = null,
  var vulnerabilities: Vulnerabilities? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") var underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  var indexOffenceDetails: String? = null,
  var fixedTermAdditionalLicenceConditions: SelectedWithDetails? = null,
  var indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails? = null,
  @JsonProperty("isMainAddressWherePersonCanBeFound") var mainAddressWherePersonCanBeFound: SelectedWithDetails? = null,
  var whyConsideredRecall: WhyConsideredRecall? = null,
  var reasonsForNoRecall: ReasonsForNoRecall? = null
) : Serializable

enum class Status {
  DRAFT, DOCUMENT_CREATED, DELETED
}

data class TextValueOption(
  val value: String? = null,
  val text: String? = null
)
