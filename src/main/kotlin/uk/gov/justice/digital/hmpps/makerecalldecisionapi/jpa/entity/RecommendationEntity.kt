package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import org.hibernate.annotations.Type
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "recommendations")
data class RecommendationEntity(
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  var data: RecommendationModel
) : EntityWithId()

data class RecommendationModel(
  var name: String? = null,
  val crn: String?,
  var recommendation: Recommendation? = null,
  var alternateActions: String? = null
) : Serializable

enum class Recommendation {
  RECALL, NOT_RECALL
}
