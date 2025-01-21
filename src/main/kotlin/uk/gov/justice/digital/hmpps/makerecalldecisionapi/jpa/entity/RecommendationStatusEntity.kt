package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import java.security.SecureRandom
import kotlin.math.abs

@Entity
@Table(name = "recommendation_status")
data class RecommendationStatusEntity(
  @Id
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var createdBy: String?,
  var createdByUserFullName: String?,
  var created: String?,
  var modifiedBy: String? = null,
  var modifiedByUserFullName: String? = null,
  var modified: String? = null,
  var name: String?,
  var active: Boolean,
  var emailAddress: String? = null,
) : Comparable<RecommendationStatusEntity> {
  override fun compareTo(other: RecommendationStatusEntity) = compareValuesBy(
    other,
    this,
  ) { it.modified ?: it.created }
}

fun RecommendationStatusEntity.toRecommendationStatusResponse(): RecommendationStatusResponse =
  RecommendationStatusResponse(
    name = name,
    recommendationId = recommendationId,
    active = active,
    created = created,
    createdBy = createdBy,
    createdByUserFullName = createdByUserFullName,
    modified = modified,
    modifiedBy = modifiedBy,
    modifiedByUserFullName = modifiedByUserFullName,
    emailAddress = emailAddress,
  )
