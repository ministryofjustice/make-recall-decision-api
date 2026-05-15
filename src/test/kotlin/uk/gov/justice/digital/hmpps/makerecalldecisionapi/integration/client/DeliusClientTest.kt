package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.contactHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.toJsonString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RegionNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

private const val TIMEOUT_IN_SECONDS = 2L // timeout defined in application-test.yml is 2 seconds

@ActiveProfiles("test")
class DeliusClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var deliusClient: DeliusClient

  @Test
  fun `find by crn`() {
    findByCrnSuccess(surname = "Bloggs")
    val response = deliusClient.findByCrn("X123456")
    assertThat(response?.name?.surname).isEqualTo("Bloggs")
  }

  @Test
  fun `find by crn returns null when not found`() {
    findByCrnNotFound(crn = "X654321")
    val response = deliusClient.findByCrn("X654321")
    assertThat(response).isNull()
  }

  @Test
  fun `find by crn - A failure other than a not found throws`() {
    val throwCrn = "X098765"
    deliusIntegration.`when`(request().withPath("/case-summary/$throwCrn")).respond(
      response().withContentType(APPLICATION_JSON)
        .withStatusCode(401)
        .withDelay(Delay.seconds(0)),
    )
    assertThatThrownBy {
      deliusClient.findByCrn(throwCrn)
    }.isInstanceOf(WebClientResponseException::class.java)
  }

  @Test
  fun `find by name`() {
    findByNameSuccess(crn = "Y654321", firstName = "Joe", surname = "Bloggs", pageNumber = 0, pageSize = 1)
    val response = deliusClient.findByName("Joe", "Bloggs", 0, 1)
    assertThat(response.content.size).isEqualTo(1)
    assertThat(response.content[0].identifiers.crn).isEqualTo("Y654321")
  }

  @Test
  fun `find by name returns no results`() {
    findByNameNoResults(firstName = "Joe", surname = "Bloggs", pageNumber = 0, pageSize = 1)
    val response = deliusClient.findByName("Joe", "Bloggs", 0, 1)
    assertThat(response.content.size).isEqualTo(0)
  }

  @Test
  fun `throws exception when no person matching crn exists`() {
    val nonExistentCrn = "X123456"
    personalDetailsNotFound(nonExistentCrn)
    assertThatThrownBy {
      deliusClient.getPersonalDetails(nonExistentCrn)
    }.isInstanceOf(PersonNotFoundException::class.java)
      .hasMessage("No details available for endpoint: /case-summary/X123456/personal-details")
  }

  @Test
  fun `throws exception on http error`() {
    personalDetailsError("X123456")
    assertThatThrownBy {
      deliusClient.getPersonalDetails("X123456")
    }.isInstanceOf(WebClientResponseException.InternalServerError::class.java)
      .hasMessage("500 Internal Server Error from GET http://localhost:8097/case-summary/X123456/personal-details")
  }

  @Test
  fun `retries on http error`() {
    personalDetailsSuccessAfterRetry("X123456")
    val personalDetails = deliusClient.getPersonalDetails("X123456")
    assertThat(personalDetails.personalDetails.name.surname).isEqualTo("Bloggs")
  }

  @Test
  fun `fetch user details`() {
    userResponse("fred", "test@digital.justice.gov.uk")
    val userDetails = deliusClient.getUserInfo("fred")
    assertThat(userDetails.email).isEqualTo("test@digital.justice.gov.uk")
  }

  @Test
  fun `fetch provider details`() {
    val code = "A11"
    val name = "Provider Name"
    providerByCodeResponse(code, name)
    val provider = deliusClient.getProvider(code)
    assertThat(provider.code).isEqualTo(code)
    assertThat(provider.name).isEqualTo(name)
  }

  @Test
  fun `given unrecognised region code when getProvider is called then RegionNotFoundException is thrown`() {
    val code = "Unrecognised"
    providerNotFoundResponse(code)
    assertThatThrownBy {
      deliusClient.getProvider(code)
    }.isInstanceOf(RegionNotFoundException::class.java)
      .hasMessage("No details available for endpoint: /provider/Unrecognised")
  }

  @Test
  fun `get contact history when no filtering options specified`() {
    // given
    val crn = randomString()

    val contactHistoryRequest = request()
      .withPath("/case-summary/$crn/contact-history")
      .withQueryStringParameters(
        mapOf(
          "query" to emptyList<String>(),
          "from" to emptyList<String>(),
          "to" to emptyList<String>(),
          "type" to emptyList<String>(),
          "includeSystemGenerated" to listOf("true"),
        ),
      )

    val contactHistory = contactHistory()
    deliusIntegration.`when`(contactHistoryRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(
          contactHistory.toJsonString(),
        ),
    )

    // when
    val response = deliusClient.getContactHistory(crn)

    // then
    assertThat(response).isEqualTo(contactHistory)
  }

  @Test
  fun `get contact history when filtering options are specified`() {
    // given
    val crn = randomString()
    val query = randomString()
    val from = randomLocalDate()
    val to = randomLocalDate()
    val typeCodes = listOf(randomString(), randomString())
    val includeSystemGenerated = randomBoolean()

    val contactHistoryRequest = request()
      .withPath("/case-summary/$crn/contact-history")
      .withQueryStringParameters(
        mapOf(
          "query" to listOf(query),
          "from" to listOf(from.toString()),
          "to" to listOf(to.toString()),
          "type" to typeCodes,
          "includeSystemGenerated" to listOf(includeSystemGenerated.toString()),
        ),
      )

    val contactHistory = contactHistory()
    deliusIntegration.`when`(contactHistoryRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(
          contactHistory.toJsonString(),
        ),
    )

    // when
    val response = deliusClient.getContactHistory(crn, query, from, to, typeCodes, includeSystemGenerated)

    // then
    assertThat(response).isEqualTo(contactHistory)
  }

  @Test
  fun `raises a PersonNotFoundException when NOT_FOUND returned by Delius`() {
    // given
    val crn = randomString()
    val contactHistoryRequest = request()
      .withPath("/case-summary/$crn/contact-history")
      .withQueryStringParameters(
        mapOf(
          "query" to emptyList<String>(),
          "from" to emptyList<String>(),
          "to" to emptyList<String>(),
          "type" to emptyList<String>(),
          "includeSystemGenerated" to listOf("true"),
        ),
      )

    deliusIntegration.`when`(contactHistoryRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withStatusCode(404),
    )

    // when / then
    assertThatThrownBy {
      deliusClient.getContactHistory(crn)
    }.isInstanceOf(PersonNotFoundException::class.java)
      .hasMessage("No details available for endpoint: /case-summary/$crn/contact-history")
  }

  @Test
  fun `raises a ClientTimeoutException when Delius times out`() {
    // given
    val crn = randomString()
    val contactHistoryRequest = request()
      .withPath("/case-summary/$crn/contact-history")
      .withQueryStringParameters(
        mapOf(
          "query" to emptyList<String>(),
          "from" to emptyList<String>(),
          "to" to emptyList<String>(),
          "type" to emptyList<String>(),
          "includeSystemGenerated" to listOf("true"),
        ),
      )

    deliusIntegration.`when`(contactHistoryRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withDelay(Delay.seconds(TIMEOUT_IN_SECONDS * 2)),
    )

    // when / then
    assertThatThrownBy {
      deliusClient.getContactHistory(crn)
    }
      .isInstanceOf(ClientTimeoutException::class.java)
      .hasMessage("Delius integration client - /case-summary/$crn/contact-history endpoint: [No response within $TIMEOUT_IN_SECONDS seconds]")
  }
}
