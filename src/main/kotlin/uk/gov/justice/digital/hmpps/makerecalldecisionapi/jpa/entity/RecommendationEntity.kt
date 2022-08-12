package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "recommendations")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RecommendationEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  open var id: Long? = null,

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
  var responseToProbation: String? = null,
  var isThisAnEmergencyRecall: Boolean? = null,
  var status: Status? = null,
  var lastModifiedBy: String? = null,
  var lastModifiedDate: String? = null,
  val createdBy: String? = null,
  val createdDate: String? = null,
  val personOnProbation: PersonOnProbation? = null
) : Serializable

enum class Status {
  DRAFT, DOCUMENT_CREATED, DELETED
}

data class TextValueOption(
  val value: String? = null,
  val text: String? = null
)
