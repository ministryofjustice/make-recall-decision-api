package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.assertMovementsAreEqual
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.prisonOffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.PrisonApiResponseMocker
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PrisonApiControllerTest : IntegrationTestBase() {

  @Value("\${prison.client.timeout}")
  var prisonTimeout: Long = 0

  // TODO use default constructor (and remove non-default one from PrisonApiResponseMocker) once we
  //      move the prisonApi val out of IntegrationTestBase as part of breaking up the latter so it
  //      is no longer a god class. This will also require calling the start-up and tear-down methods
  private val prisonApiResponseMocker: PrisonApiResponseMocker = PrisonApiResponseMocker(prisonApi)

  @Test
  fun `retrieves offender details`() {
    runTest {
      val nomsId = "A123456"
      val locationDescription = "Leeds"
      val facialImageId = "1234"
      val agencyId = "BRX"
      prisonApiOffenderMatchResponse(nomsId, locationDescription, facialImageId, agencyId)
      prisonApiImageResponse(facialImageId, "data")
      val agencyDescription = "HMP Brixton"
      mockPrisonApiAgencyResponse(agencyId, agency(description = agencyDescription))

      val response = convertResponseToJSONObject(
        webTestClient.post()
          .uri("/prison-offender-search")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(prisonOffenderSearchRequest(nomsId)),
          )
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))))
          }
          .exchange()
          .expectStatus().isOk,
      )
      assertThat(response.get("locationDescription")).isEqualTo(locationDescription)
      assertThat(response.get("agencyDescription")).isEqualTo(agencyDescription)
    }
  }

  @Test
  fun `retrieves offender movements`() {
    runTest {
      // given
      val nomsId = "A123456"
      val prisonApiMovements =
        listOf(prisonApiOffenderMovement(), prisonApiOffenderMovement(), prisonApiOffenderMovement())
      prisonApiResponseMocker.mockRetrieveOffenderMovementsResponse(nomsId, prisonApiMovements)

      // when
      val response = convertResponseToJSONArray(
        webTestClient.get()
          .uri("/offenders/$nomsId/movements")
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_PPCS"))))
          }
          .exchange()
          .expectStatus().isOk,
      )

      // then
      val jacksonTypeReference: TypeReference<List<OffenderMovement>> =
        object : TypeReference<List<OffenderMovement>>() {}
      val movements = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
      assertThat(movements).hasSameSizeAs(prisonApiMovements)
      for (i in movements.indices) {
        assertMovementsAreEqual(movements[i], prisonApiMovements[i])
      }
    }
  }

  @Test
  fun `offender movement retrieval fails due to missing authorisation`() {
    runTest {
      // given
      val nomsId = "A123456"

      // when then
      webTestClient.get()
        .uri("/offenders/$nomsId/movements")
        .headers {
          (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))))
        }
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Test
  fun `offender movement retrieval fails due to Prison API timeouts`() {
    runTest {
      // given
      val nomsId = "A123456"
      val timeoutMessage = "Prison API Client: [No response within $prisonTimeout seconds]"
      val expectedErrorResponse = ErrorResponse(
        status = GATEWAY_TIMEOUT,
        userMessage = "Client timeout: $timeoutMessage",
        developerMessage = timeoutMessage,
      )
      prisonApiResponseMocker.mockRetrieveOffenderMovementsTimeout(nomsId, prisonTimeout)

      // when
      val response = convertResponseToJSONObject(
        webTestClient.get()
          .uri("/offenders/$nomsId/movements")
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_PPCS"))))
          }
          .exchange()
          .expectStatus().isEqualTo(GATEWAY_TIMEOUT),
      )

      // then
      val jacksonTypeReference: TypeReference<ErrorResponse> =
        object : TypeReference<ErrorResponse>() {}
      val actualErrorResponse = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
      assertThat(actualErrorResponse).isEqualTo(expectedErrorResponse)
    }
  }
}
