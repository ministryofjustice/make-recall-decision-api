package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
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
)

data class RecommendationModel(
  val crn: String?,
  var recommendation: Recommendation? = null,
  var status: Status? = null,
  var lastModifiedBy: String? = null,
  val lastModifiedDate: String? = null
) : Serializable

enum class Recommendation(val text: String) {
  STANDARD("Standard"), NO_RECALL("No recall"), FIXED_TERM("Fixed term"),
}

enum class Status {
  DRAFT
}

data class RecallTypeOption(
  val value: String? = null,
  val text: String? = null
)
