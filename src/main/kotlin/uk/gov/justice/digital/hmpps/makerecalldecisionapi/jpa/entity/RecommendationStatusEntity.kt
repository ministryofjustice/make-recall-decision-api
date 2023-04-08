package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import java.security.SecureRandom
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recommendation_status")
data class RecommendationStatusEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var createdBy: String?,
  var createdByUserName: String?,
  var created: String?,
  var modifiedBy: String? = null,
  var modifiedByUserName: String? = null,
  var modified: String? = null,
  var status: String?,
  var active: Boolean
)
fun RecommendationStatusEntity.toRecommendationStatusResponse(): RecommendationStatusResponse =
  RecommendationStatusResponse(
    status = status,
    recommendationId = recommendationId,
    active = active,
    created = created,
    createdBy = createdBy,
    createdByUserName = createdByUserName,
    modified = modified,
    modifiedBy = modifiedBy,
    modifiedByUserName = modifiedByUserName
  )
