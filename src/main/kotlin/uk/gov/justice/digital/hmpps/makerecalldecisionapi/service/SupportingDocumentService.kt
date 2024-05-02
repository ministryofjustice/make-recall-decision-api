package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentMetaDataResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toSupportingDocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
internal class SupportingDocumentService(
  val recommendationDocumentRepository: RecommendationSupportingDocumentRepository,
  val documentManagementClient: DocumentManagementClient,
  @Lazy val recommendationRepository: RecommendationRepository,
) {
  fun fetchSupportingDocuments(recommendationId: Long): List<SupportingDocumentMetaDataResponse> {
    return recommendationDocumentRepository.findByRecommendationId(recommendationId)
      .map { it.toSupportingDocumentResponse() }
  }

  fun uploadNewSupportingDocument(
    recommendationId: Long,
    type: String,
    title: String,
    mimetype: String,
    filename: String,
    created: String,
    createdBy: String?,
    createdByUserFullName: String?,
    data: String,
    flags: FeatureFlags,
  ): Long {
    val crn = recommendationRepository.findById(recommendationId).getOrNull()?.data?.crn
    val documentUuid = uploadFile(filename, data, crn)
    val result = recommendationDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = recommendationId,
        mimetype = mimetype,
        type = type,
        title = title,
        filename = filename,
        created = created,
        createdByUserFullName = createdByUserFullName,
        createdBy = createdBy,
        uploaded = created,
        uploadedBy = createdBy,
        uploadedByUserFullName = createdByUserFullName,
        documentUuid = documentUuid,
        data = Base64.getDecoder().decode(data),
      ),
    )
    return result.id
  }

  fun replaceSupportingDocument(
    id: Long,
    title: String,
    mimetype: String,
    filename: String,
    uploaded: String,
    uploadedBy: String?,
    uploadedByUserFullName: String?,
    data: String,
    flags: FeatureFlags,
  ) {
    val file =
      recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }

    file.title = title
    file.mimetype = mimetype
    file.filename = filename
    file.uploaded = uploaded
    file.uploadedBy = uploadedBy
    file.uploadedByUserFullName = uploadedByUserFullName
    file.data = Base64.getDecoder().decode(data)

    recommendationDocumentRepository.save(file)
  }

  fun removeSupportingDocument(id: Long, flags: FeatureFlags) {
    val file =
      recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }

    recommendationDocumentRepository.delete(file)
  }

  fun getSupportingDocument(id: Long, flags: FeatureFlags): SupportingDocumentResponse {
    val file =
      recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }

    val encodedString: String = Base64.getEncoder().encodeToString(file.data)

    return SupportingDocumentResponse(
      recommendationId = file.recommendationId,
      id = id,
      data = encodedString,
      type = file.type,
      filename = file.filename,
    )
  }

  private fun uploadFile(
    filename: String,
    data: String,
    crn: String?,
  ): UUID? {
    val raw = Base64.getDecoder().decode(data)
    return getValueAndHandleWrappedException(documentManagementClient.uploadFile(crn, raw, filename))
  }
}
