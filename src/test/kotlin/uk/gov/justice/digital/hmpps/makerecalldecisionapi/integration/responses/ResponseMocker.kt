package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

import com.google.gson.Gson
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Delay.seconds
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.mockserver.model.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.toJsonBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.toJsonString

internal abstract class ResponseMocker {

  protected val clientAndServer: ClientAndServer

  private val gson: Gson = Gson()

  protected constructor(clientAndServer: ClientAndServer) {
    this.clientAndServer = clientAndServer
  }

  protected constructor(port: Int) {
    clientAndServer = ClientAndServer.startClientAndServer(port)
  }

  fun startUpServer() {
    resetServer()
  }

  fun resetServer() {
    clientAndServer.reset()
  }

  fun tearDownServer() {
    // The stop method should only return once the server and client are fully stopped, but when running the full test
    // suite we're getting issues between tests trying to start a mock server on the same port. Since the default JUnit
    // behaviour is not to run test classes in parallel, this suggests the stop method is returning too early. It seems
    // we aren't the only ones seeing this behaviour: https://github.com/mock-server/mockserver/issues/1097
    do {
      clientAndServer.stop()
    } while (!clientAndServer.hasStopped())
  }

  fun setUpSuccessfulHealthCheck() {
    clientAndServer
      .`when`(request().withPath("/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "UP"))),
      )
  }

  fun setUpFailingHealthCheck() {
    clientAndServer
      .`when`(request().withPath("/health/ping"))
      .error(HttpError.error())
  }

  protected fun mockResponse(
    path: String,
    requestBody: Any? = null,
    responseBody: Any? = null,
    delaySeconds: Long = 0,
    mediaType: MediaType = APPLICATION_JSON,
  ) {
    val request = if (requestBody == null) {
      request().withPath(path)
    } else {
      request().withPath(path).withBody(toJsonBody(requestBody))
    }

    if (responseBody == null) {
      clientAndServer.`when`(request).respond(
        response().withContentType(mediaType)
          .withDelay(seconds(delaySeconds)),
      )
    }
    clientAndServer.`when`(request).respond(
      response().withContentType(mediaType)
        .withBody(toJsonString(responseBody))
        .withDelay(seconds(delaySeconds)),
    )
  }

  protected fun mockTimeout(
    path: String,
    timeoutInSeconds: Long,
  ) {
    val request = request().withPath(path)

    clientAndServer.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody("")
        .withDelay(seconds(timeoutInSeconds * 3)),
    )
  }
}
