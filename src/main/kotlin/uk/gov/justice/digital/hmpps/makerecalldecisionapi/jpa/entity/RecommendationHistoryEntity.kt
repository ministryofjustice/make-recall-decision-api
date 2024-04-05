package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
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
  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb")
  var recommendation: RecommendationModel,
) : Comparable<RecommendationHistoryEntity> {
  override fun compareTo(other: RecommendationHistoryEntity) = compareValuesBy(
    other,
    this,
  ) { it.recommendation.lastModifiedDate }
}
