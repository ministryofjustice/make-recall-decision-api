package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ppudautomation

import org.mockserver.integration.ClientAndServer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreatedOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreatedSentence
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ResponseMocker

internal class PpudAutomationResponseMocker : ResponseMocker {

  private val ppudAutomationApi: ClientAndServer = clientAndServer

  // port aligned with those specified in application-test.yml
  constructor() : super(8099)

  fun ppudAutomationSearchApiMatchResponse(
    ppudSearchRequest: PpudSearchRequest,
    ppudSearchResponse: PpudSearchResponse,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/search",
      requestBody = ppudSearchRequest,
      responseBody = ppudSearchResponse,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationDetailsMatchResponse(
    ppudDetailsResponse: PpudDetailsResponse,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/${ppudDetailsResponse.offender.id}",
      responseBody = ppudDetailsResponse,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationBookRecallApiMatchResponse(
    nomsId: String,
    ppudCreateRecallRequest: PpudCreateRecallRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$nomsId/recall",
      requestBody = ppudCreateRecallRequest,
      responseBody = PpudCreateRecallResponse(PpudCreateRecall(id)),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationCreateOffenderApiMatchResponse(
    id: String,
    createOffenderRequest: PpudCreateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender",
      requestBody = createOffenderRequest,
      responseBody = PpudCreateOffenderResponse(PpudCreatedOffender(id)),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUpdateOffenderApiMatchResponse(
    offenderId: String,
    updateOffenderRequest: PpudUpdateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId",
      requestBody = updateOffenderRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationCreateSentenceApiMatchResponse(
    offenderId: String,
    createSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId/sentence",
      requestBody = createSentenceRequest,
      responseBody = PpudCreateSentenceResponse(PpudCreatedSentence(id)),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUpdateSentenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId/sentence/$sentenceId",
      requestBody = updateSentenceRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUpdateOffenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    ppudUpdateOffenceRequest: PpudUpdateOffenceRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId/sentence/$sentenceId/offence",
      requestBody = ppudUpdateOffenceRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUpdateReleaseApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateReleaseRequest: PpudCreateOrUpdateReleaseRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId/sentence/$sentenceId/release",
      requestBody = updateReleaseRequest,
      responseBody = PpudCreateOrUpdateReleaseResponse(PpudCreatedOrUpdatedRelease(id)),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationCreateRecallApiMatchResponse(
    offenderId: String,
    releaseId: String,
    id: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/offender/$offenderId/release/$releaseId/recall",
      responseBody = PpudCreateRecallResponse(PpudCreateRecall(id)),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUploadMandatoryDocumentApiMatchResponse(
    recallId: String,
    ppudUploadMandatoryDocumentRequest: PpudUploadMandatoryDocumentRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/recall/$recallId/mandatory-document",
      requestBody = ppudUploadMandatoryDocumentRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationUploadAdditionalDocumentApiMatchResponse(
    recallId: String,
    ppudUploadAdditionalDocumentRequest: PpudUploadAdditionalDocumentRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/recall/$recallId/additional-document",
      requestBody = ppudUploadAdditionalDocumentRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationCreateMinuteApiMatchResponse(
    recallId: String,
    ppudCreateMinuteRequest: PpudCreateMinuteRequest,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/recall/$recallId/minutes",
      requestBody = ppudCreateMinuteRequest,
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationReferenceListApiMatchResponse(
    name: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/reference/$name",
      responseBody = PpudReferenceListResponse(listOf("one", "two")),
      delaySeconds = delaySeconds,
    )
  }

  fun ppudAutomationSearchActiveUsersApiMatchResponse(
    ppudUserSearchRequest: PpudUserSearchRequest,
    userFullName: String,
    teamName: String,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/user/search",
      requestBody = ppudUserSearchRequest,
      responseBody = PpudUserResponse(listOf(PpudUser(userFullName, teamName))),
      delaySeconds = delaySeconds,
    )
  }
}
