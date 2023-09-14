package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationFileResponse
import java.security.SecureRandom
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recommendation_file")
data class RecommendationFileEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long,
  var createdBy: String,
  var createdByUserFullName: String,
  var created: String,
  var token: String,
  var category: String,
  var name: String? = null,
  @Column(name = "s3_id")
  var s3Id: UUID? = null,
  var type: String? = null,
  var size: Long? = null,
  var notes: String? = null,
)


fun RecommendationFileEntity.toRecommendationFileResponse(): RecommendationFileResponse =
  RecommendationFileResponse(
    id = id,
    recommendationId = recommendationId,
    created = created,
    createdBy = createdBy,
    createdByUserFullName = createdByUserFullName,
    token = token,
    category = category,
    name = name,
    s3Id = s3Id,
    type = type,
    size = size,
    notes = notes
  )


