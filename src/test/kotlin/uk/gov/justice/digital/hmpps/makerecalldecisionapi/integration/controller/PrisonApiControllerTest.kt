package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.PrisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.toJsonString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.prisonOffenderSearchRequest
import java.time.LocalDateTime

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PrisonApiControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves offender details`() {
    runTest {
      val nomsId = "A123456"
      val locationDescription = "Leeds, clearly Leeds"
      val facialImageId = "1234"
      val agencyId = "KLN"
      prisonApiOffenderMatchResponse(nomsId, locationDescription, facialImageId, agencyId)
      prisonApiImageResponse(facialImageId, "data")
      val agencyDescription = "The Kyln"
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
      mockPrisonApiOffenderMovementsResponse(nomsId, prisonApiMovements)

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
      assertThat(response.length()).isEqualTo(prisonApiMovements.size)
      for (i in 0..<response.length()) {
        assertJsonMovementIsEqualToExpectedMovement(response.getJSONObject(i), prisonApiMovements[i])
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

  private fun mockPrisonApiOffenderMovementsResponse(
    nomsId: String,
    movements: List<PrisonApiOffenderMovement>,
  ) {
    val request = request().withPath("/api/movements/offender/$nomsId")

    prisonApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(movements.joinToString(",", "[", "]") { it.toJsonString() }),
    )
  }

  private fun assertJsonMovementIsEqualToExpectedMovement(
    jsonMovement: JSONObject,
    prisonApiMovement: PrisonApiOffenderMovement,
  ) {
    assertThat(jsonMovement.get("offenderNo")).isEqualTo(prisonApiMovement.offenderNo)
    assertThat(jsonMovement.get("fromAgency")).isEqualTo(prisonApiMovement.fromAgency)
    assertThat(jsonMovement.get("fromAgencyDescription")).isEqualTo(prisonApiMovement.fromAgencyDescription)
    assertThat(jsonMovement.get("toAgency")).isEqualTo(prisonApiMovement.toAgency)
    assertThat(jsonMovement.get("toAgencyDescription")).isEqualTo(prisonApiMovement.toAgencyDescription)
    assertThat(jsonMovement.get("movementType")).isEqualTo(prisonApiMovement.movementType)
    assertThat(jsonMovement.get("movementTypeDescription")).isEqualTo(prisonApiMovement.movementTypeDescription)
    assertThat(jsonMovement.get("movementDateTime")).isEqualTo(
      LocalDateTime.of(
        prisonApiMovement.movementDate,
        prisonApiMovement.movementTime,
      ),
    )
  }
}
