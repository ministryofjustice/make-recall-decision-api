package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationEventsRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RecFlowEventControllerTest() : IntegrationTestBase() {

  @Test
  fun `creates a recommendation flow event`() {
    runTest {
      val response = convertResponseToJSONObject(
        webTestClient.post()
          .uri("/recommendations/events")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(recommendationEventsRequest(crn))
          )
          .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
          .exchange()
          .expectStatus().isCreated
      )
      assertThat(response.get("id")).isNotNull
      assertThat(response.get("crn")).isEqualTo(crn)
      assertThat(response.get("userId")).isEqualTo("Bill")
      assertThat(response.get("timeStamp")).isEqualTo("2022-09-12T15:00:08")
      assertThat(response.get("eventType")).isEqualTo("SEARCH_RESULT_CLICKED")
    }
  }
}
