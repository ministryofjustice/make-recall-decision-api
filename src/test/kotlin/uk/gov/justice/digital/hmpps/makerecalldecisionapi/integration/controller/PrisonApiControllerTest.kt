package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.prisonOffenderSearchRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PrisonApiControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves offender details`() {
    runTest {
      val nomsId = "A123456"
      prisonApiOffenderMatchResponse(nomsId, "Leeds, clearly Leeds", "1234")
      prisonApiImageResponse("1234", "data")

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
      assertThat(response.get("locationDescription")).isEqualTo("Leeds, clearly Leeds")
    }
  }
}
