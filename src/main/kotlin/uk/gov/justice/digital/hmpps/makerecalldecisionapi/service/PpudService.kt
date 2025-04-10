package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ppud.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentCategory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertToLondonTimezone

@Service
internal class PpudService(
  @Qualifier("ppudAutomationApiClient") private val ppudAutomationApiClient: PpudAutomationApiClient,
  private val ppudUserMappingRepository: PpudUserMappingRepository,
  private val recommendationDocumentRepository: RecommendationSupportingDocumentRepository,
) {
  fun search(request: PpudSearchRequest): PpudSearchResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.search(request),
    )
    return response!!
  }

  fun details(id: String): PpudDetailsResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.details(id),
    )
    return response!!
  }

  fun retrieveList(name: String): PpudReferenceListResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.retrieveList(name),
    )
    return response!!
  }

  fun createOffender(request: PpudCreateOffenderRequest): PpudCreateOffenderResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.createOffender(request),
    )
    return response!!
  }

  fun createSentence(offenderId: String, request: PpudCreateOrUpdateSentenceRequest): PpudCreateSentenceResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.createSentence(offenderId, request),
    )
    return response!!
  }

  fun updateSentence(offenderId: String, sentenceId: String, request: PpudCreateOrUpdateSentenceRequest) {
    getValueAndHandleWrappedException(
      ppudAutomationApiClient.updateSentence(offenderId, sentenceId, request),
    )
  }

  fun updateOffence(offenderId: String, sentenceId: String, request: PpudUpdateOffenceRequest) {
    getValueAndHandleWrappedException(
      ppudAutomationApiClient.updateOffence(offenderId, sentenceId, request),
    )
  }

  fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: PpudCreateOrUpdateReleaseRequest,
  ): PpudCreateOrUpdateReleaseResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.createOrUpdateRelease(offenderId, sentenceId, createOrUpdateReleaseRequest),
    )
    return response!!
  }

  fun createRecall(
    offenderId: String,
    releaseId: String,
    createRecallRequest: CreateRecallRequest,
    username: String,
  ): PpudCreateRecallResponse {
    val ppudUser =
      ppudUserMappingRepository.findByUserNameIgnoreCase(username)?.let { PpudUser(it.ppudUserFullName, it.ppudTeamName) }
        ?: throw NotFoundException("PPUD user mapping not found for username '$username'")

    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.createRecall(
        offenderId,
        releaseId,
        PpudCreateRecallRequest(
          decisionDateTime = convertToLondonTimezone(createRecallRequest.decisionDateTime),
          isExtendedSentence = createRecallRequest.isExtendedSentence,
          isInCustody = createRecallRequest.isInCustody,
          mappaLevel = createRecallRequest.mappaLevel,
          policeForce = createRecallRequest.policeForce,
          probationArea = createRecallRequest.probationArea,
          receivedDateTime = convertToLondonTimezone(createRecallRequest.receivedDateTime),
          recommendedTo = ppudUser,
          riskOfContrabandDetails = createRecallRequest.riskOfContrabandDetails,
        ),
      ),
    )
    return response!!
  }

  fun updateOffender(offenderId: String, request: PpudUpdateOffenderRequest) {
    getValueAndHandleWrappedException(
      ppudAutomationApiClient.updateOffender(offenderId, request),
    )
  }

  fun uploadMandatoryDocument(
    recallId: String,
    uploadMandatoryDocument: UploadMandatoryDocumentRequest,
    username: String,
  ) {
    val ppudUser =
      ppudUserMappingRepository.findByUserNameIgnoreCase(username)?.let { PpudUser(it.ppudUserFullName, it.ppudTeamName) }
        ?: throw NotFoundException("PPUD user mapping not found for username '$username'")

    val category = when (uploadMandatoryDocument.category) {
      "PPUDPartA" -> DocumentCategory.PartA
      "PPUDLicenceDocument" -> DocumentCategory.Licence
      "PPUDProbationEmail" -> DocumentCategory.RecallRequestEmail
      "PPUDOASys" -> DocumentCategory.OASys
      "PPUDPrecons" -> DocumentCategory.PreviousConvictions
      "PPUDPSR" -> DocumentCategory.PreSentenceReport
      "PPUDChargeSheet" -> DocumentCategory.ChargeSheet
      else -> {
        throw InvalidRequestException("Invalid document category to upload: " + uploadMandatoryDocument.category)
      }
    }

    val doc = recommendationDocumentRepository.findById(uploadMandatoryDocument.id)
      .orElseThrow { NotFoundException("Supporting document not found") }

    getValueAndHandleWrappedException(
      ppudAutomationApiClient.uploadMandatoryDocument(
        recallId,
        PpudUploadMandatoryDocumentRequest(
          documentId = doc.documentUuid!!,
          category = category,
          owningCaseworker = ppudUser,
        ),
      ),
    )
  }

  fun uploadAdditionalDocument(
    recallId: String,
    uploadMandatoryDocument: UploadAdditionalDocumentRequest,
    username: String,
  ) {
    val ppudUser =
      ppudUserMappingRepository.findByUserNameIgnoreCase(username)?.let { PpudUser(it.ppudUserFullName, it.ppudTeamName) }
        ?: throw NotFoundException("PPUD user mapping not found for username '$username'")

    val doc = recommendationDocumentRepository.findById(uploadMandatoryDocument.id)
      .orElseThrow { NotFoundException("Supporting document not found") }

    getValueAndHandleWrappedException(
      ppudAutomationApiClient.uploadAdditionalDocument(
        recallId,
        PpudUploadAdditionalDocumentRequest(
          documentId = doc.documentUuid!!,
          title = doc.title!!,
          owningCaseworker = ppudUser,
        ),
      ),
    )
  }

  fun createMinute(
    recallId: String,
    createMinuteRequest: CreateMinuteRequest,
    username: String,
  ) {
    getValueAndHandleWrappedException(
      ppudAutomationApiClient.createMinute(
        recallId,
        PpudCreateMinuteRequest(
          subject = createMinuteRequest.subject,
          text = createMinuteRequest.text,
        ),
      ),
    )
  }

  fun searchActiveUsers(request: PpudUserSearchRequest): PpudUserResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.searchActiveUsers(request),
    )
    return response!!
  }
}
