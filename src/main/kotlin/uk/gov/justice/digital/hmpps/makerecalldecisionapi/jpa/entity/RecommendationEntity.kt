package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "recommendations")
data class RecommendationEntity(
  var name: String?,
  val crn: String?,
  var recommendation: Recommendation,
  var alternateActions: String
) : EntityWithUUID()

enum class Recommendation {
  RECALL, NOT_RECALL
}
