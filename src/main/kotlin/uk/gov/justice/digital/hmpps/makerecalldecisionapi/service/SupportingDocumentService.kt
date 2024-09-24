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
import java.util.Base64
import java.util.UUID
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
    val documentUuid = uploadFile(filename, data, crn, UUID.randomUUID().toString(), mimetype)
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
      ),
    )
    return result.id
  }

  fun replaceSupportingDocument(
    id: Long,
    title: String,
    uploaded: String,
    uploadedBy: String?,
    uploadedByUserFullName: String?,
    mimetype: String?,
    filename: String?,
    data: String?,
    flags: FeatureFlags,
  ) {
    val file = recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }
    val crn = file.recommendationId?.run {recommendationRepository.findById}?.getOrNull()?.data.crn

    if (data != null && filename != null) {
      recommendationDocumentRepository.delete(file)
      getValueAndHandleWrappedException(documentManagementClient.deleteFile(file.documentUuid.toString()))
      file.documentUuid = uploadFile(filename = filename, data = data, crn = crn, documentUuid = UUID.randomUUID().toString(), mimetype = mimetype)
    }
    file.title = title
    file.mimetype = mimetype ?: file.mimetype
    file.filename = filename ?: file.filename
    file.uploaded = uploaded
    file.uploadedBy = uploadedBy
    file.uploadedByUserFullName = uploadedByUserFullName

    recommendationDocumentRepository.save(file)
  }

  fun removeSupportingDocument(id: Long, flags: FeatureFlags) {
    val file =
      recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }
    recommendationDocumentRepository.delete(file)
    getValueAndHandleWrappedException(documentManagementClient.deleteFile(file.documentUuid.toString()))
  }

  fun getSupportingDocument(id: Long, flags: FeatureFlags): SupportingDocumentResponse {
    val file = recommendationDocumentRepository.findById(id).orElseThrow { NotFoundException("Supporting document not found") }
    val bytes = getValueAndHandleWrappedException(
      documentManagementClient.downloadFileAsByteArray(documentUuid = file.documentUuid.toString(), filename = file.filename),
    )
    val encodedString: String = Base64.getEncoder().encodeToString(bytes)

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
    documentUuid: String,
    mimetype: String? = null,
  ): UUID? {
    val raw = Base64.getDecoder().decode(data)
    return getValueAndHandleWrappedException(documentManagementClient.uploadFile(crn, raw, filename, documentUuid, mimetype))
  }
}
