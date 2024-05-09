package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertToLondonTimezone

@Service
internal class PpudService(
  @Qualifier("ppudAutomationApiClient") private val ppudAutomationApiClient: PpudAutomationApiClient,
  private val ppudUserRepository: PpudUserRepository,
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

  fun bookToPpud(nomisId: String, payload: PpudBookRecall): PpudBookRecallResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.bookToPpud(nomisId, payload),
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
    val ppudUser = ppudUserRepository.findByUserNameIgnoreCase(username)?.let { PpudUser(it.ppudUserFullName, it.ppudTeamName) }
      ?: throw NotFoundException("PPUD user not found for username '$username'")

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
          riskOfSeriousHarmLevel = createRecallRequest.riskOfSeriousHarmLevel,
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
}
