package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

class EstablishmentMappingControllerTest : IntegrationTestBase() {

  @Test
  fun `get all establishment mappings`() {
    runTest {
      val actualMappings =
        convertResponseToJSONObject(
          getEstablishmentMappings()
            .expectStatus().isOk,
        )

      assertThat(actualMappings.keySet()).contains("establishmentMappings")
      val actualEstablishmentMappingsJsonNode = actualMappings.getJSONObject("establishmentMappings")
      val actualEstablishmentMappings = actualEstablishmentMappingsJsonNode.toMap()
      assertThat(actualEstablishmentMappings).isNotEmpty
      actualEstablishmentMappings.entries.forEach { entry ->
        assertThat(entry.key).isNotNull()
        assertThat(entry.value).isNotNull()
      }
    }
  }

  @Test
  fun `get all establishment mappings fails if missing role`() {
    runTest {
      convertResponseToJSONObject(
        getEstablishmentMappings(roles = listOf("ROLE_MAKE_RECALL_DECISION"))
          .expectStatus().isForbidden,
      )
    }
  }

  private fun getEstablishmentMappings(roles: List<String> = listOf("ROLE_MAKE_RECALL_DECISION_PPCS")) =
    webTestClient.get()
      .uri("/establishment-mappings")
      .headers { it.authToken(roles) }
      .exchange()
}
