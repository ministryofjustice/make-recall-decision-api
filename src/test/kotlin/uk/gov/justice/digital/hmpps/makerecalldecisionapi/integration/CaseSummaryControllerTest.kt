package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CaseSummaryControllerTest(
//  @Autowired private val objectMapper: ObjectMapper
) : IntegrationTestBase() {

  @Test
  fun `retrieves stuff`() {//TODO start mockserver
    runBlockingTest {
      val crn = "1565"
      webTestClient.get()
        .uri { builder ->
          builder.path("/cases/${crn}/search").build()
        }
        .headers { it.authToken(roles = listOf("MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    }
  }

  //TODO test for should not work with wrong role!

}