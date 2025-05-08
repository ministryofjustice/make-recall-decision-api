package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ppudautomation

import com.google.gson.Gson
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.Delay
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppud.toJsonBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toJson
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toJsonString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationSearchActiveUsersResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationUpdateReleaseResponse

@Service
class PpudAutomationResponseMocker {

  val ppudAutomationApi: ClientAndServer = startClientAndServer(8099)
  private val gson: Gson = Gson()

  fun startUpServer() {
    resetServer()
  }

  fun resetServer() {
    ppudAutomationApi.reset()
  }

  fun tearDownServer() {
    ppudAutomationApi.stop()
  }

  fun setUpSuccessfulHealthCheck() {
    ppudAutomationApi
      .`when`(request().withPath("/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "UP"))),
      )
  }

  fun setUpFailingHealthCheck() {
    ppudAutomationApi
      .`when`(request().withPath("/health/ping"))
      .error(HttpError.error())
  }

  fun ppudAutomationSearchApiMatchResponse(
    nomsId: String,
    croNumber: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/search")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationSearchResponse(nomsId, croNumber))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationDetailsMatchResponse(
    ppudDetailsResponse: PpudDetailsResponse,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/${ppudDetailsResponse.offender.id}")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudDetailsResponse.toJsonString())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationBookRecallApiMatchResponse(
    nomsId: String,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$nomsId/recall")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationBookRecallResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationCreateOffenderApiMatchResponse(
    id: String,
    createOffenderRequest: PpudCreateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender").withBody(createOffenderRequest.toJsonBody())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateOffenderResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUpdateOffenderApiMatchResponse(
    offenderId: String,
    updateOffenderRequest: PpudUpdateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$offenderId").withBody(updateOffenderRequest.toJsonBody())

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationCreateSentenceApiMatchResponse(
    offenderId: String,
    createSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$offenderId/sentence").withBody(createSentenceRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateSentenceResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUpdateSentenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    delaySeconds: Long = 0,
  ) {
    val request =
      request().withPath("/offender/$offenderId/sentence/$sentenceId").withBody(updateSentenceRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUpdateOffenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/" + offenderId + "/sentence/" + sentenceId + "/offence")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUpdateReleaseApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateReleaseRequest: PpudCreateOrUpdateReleaseRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request =
      request().withPath("/offender/$offenderId/sentence/$sentenceId/release").withBody(updateReleaseRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationUpdateReleaseResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationCreateRecallApiMatchResponse(
    offenderId: String,
    releaseId: String,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/" + offenderId + "/release/" + releaseId + "/recall")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateRecallResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUploadMandatoryDocumentApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/mandatory-document")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationUploadAdditionalDocumentApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/additional-document")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationCreateMinuteApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/minutes")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationReferenceListApiMatchResponse(
    name: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/reference/$name")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody("{ \"values\": [\"one\",\"two\"] }")
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  fun ppudAutomationSearchActiveUsersApiMatchResponse(
    userFullName: String,
    userName: String,
    teamName: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/user/search")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationSearchActiveUsersResponse(userFullName, teamName))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }
}