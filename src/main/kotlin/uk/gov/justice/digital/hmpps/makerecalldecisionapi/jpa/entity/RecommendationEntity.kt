package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "recommendations")
data class RecommendationEntity(
  @Id
  @Column
  @GeneratedValue(strategy = IDENTITY)
  val id: Long? = null,

  @Column
  var name: String?,

  @Column
  @NotNull
  val crn: String,

  @Column(name = "recommendation")
  @Enumerated(EnumType.STRING)
  var recommendation: Recommendation,

  @Column(name = "alternate_actions")
  var alternateActions: String
)

enum class Recommendation {
  RECALL, NOT_RECALL
}
