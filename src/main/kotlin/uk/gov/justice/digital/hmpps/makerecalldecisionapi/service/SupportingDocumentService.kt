package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toSupportingDocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository

@Service
internal class SupportingDocumentService(
  val recommendationDocumentRepository: RecommendationSupportingDocumentRepository,
) {
  fun fetchSupportingDocuments(recommendationId: Long): List<SupportingDocumentResponse> {
    return recommendationDocumentRepository.findByRecommendationId(recommendationId)
      .map { it.toSupportingDocumentResponse() }
  }

  fun uploadNewSupportingDocument(
    recommendationId: Long,
    type: String,
    mimetype: String,
    filename: String,
    created: String,
    createdBy: String?,
    createdByUserFullName: String?,
    data: String,
    flags: FeatureFlags,
  ) {
    recommendationDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = recommendationId,
        mimetype = mimetype,
        type = type,
        filename = filename,
        created = created,
        createdByUserFullName = createdByUserFullName,
        createdBy = createdBy,
        uploaded = created,
        uploadedBy = createdBy,
        uploadedByUserFullName = createdByUserFullName,
        data = java.util.Base64.getDecoder().decode(data),
      ),
    )
  }
}
