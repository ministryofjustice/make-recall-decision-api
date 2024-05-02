package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import jakarta.persistence.Basic
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentMetaDataResponse
import java.security.SecureRandom
import java.util.UUID
import kotlin.math.abs

@Entity
@Table(name = "recommendation_document")
data class RecommendationSupportingDocumentEntity(
  @Id
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  var recommendationId: Long?,
  var createdBy: String?,
  var createdByUserFullName: String?,
  var created: String?,
  var filename: String?,
  var mimetype: String?,
  var type: String?,
  var title: String?,
  var uploadedBy: String? = null,
  var uploadedByUserFullName: String? = null,
  var uploaded: String? = null,
  var documentUuid: UUID? = null,

  @Basic(fetch = FetchType.LAZY)
  var data: ByteArray,
)

fun RecommendationSupportingDocumentEntity.toSupportingDocumentResponse(): SupportingDocumentMetaDataResponse =
  SupportingDocumentMetaDataResponse(
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
    documentUuid = documentUuid,
    title = title,
  )
