package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.security.SecureRandom
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recommendation_history")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RecommendationHistoryEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var modifiedBy: String? = null,
  var modifiedByUserFullName: String? = null,
  var modified: String? = null,
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  var recommendation: RecommendationModel,
) : Comparable<RecommendationHistoryEntity> {
  override fun compareTo(other: RecommendationHistoryEntity) = compareValuesBy(
    other,
    this,
  ) { it.recommendation.lastModifiedDate }
}
