package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentResponse
import java.security.SecureRandom
import javax.persistence.Basic
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recommendation_document")
data class RecommendationSupportingDocumentEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var createdBy: String?,
  var createdByUserFullName: String?,
  var created: String?,
  var filename: String?,
  var mimetype: String?,
  var type: String?,
  var uploadedBy: String? = null,
  var uploadedByUserFullName: String? = null,
  var uploaded: String? = null,

  @Basic(fetch = FetchType.LAZY)
  var data: ByteArray,
)

fun RecommendationSupportingDocumentEntity.toSupportingDocumentResponse(): SupportingDocumentResponse =
  SupportingDocumentResponse(
    id = id,
    recommendationId = recommendationId,
    createdBy = createdBy,
    createdByUserFullName = createdByUserFullName,
    created = created,
    filename = filename,
    type = type,
    uploadedBy = uploadedBy,
    uploadedByUserFullName = uploadedByUserFullName,
    uploaded = uploaded,
  )
