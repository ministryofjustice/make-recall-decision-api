package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.prisonOffenderSearchRequest

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
}
