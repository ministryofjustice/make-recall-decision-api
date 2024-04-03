package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.security.SecureRandom
import kotlin.math.abs

@Entity
@Table(name = "recommendation_history")
data class RecommendationHistoryEntity(
  @Id
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var modifiedBy: String? = null,
  var modifiedByUserFullName: String? = null,
  var modified: String? = null,
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  var recommendation: RecommendationModel,
) : Comparable<RecommendationHistoryEntity> {
  override fun compareTo(other: RecommendationHistoryEntity) = compareValuesBy(
    other,
    this,
  ) { it.recommendation.lastModifiedDate }
}
