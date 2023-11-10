package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate

@ActiveProfiles("test")
class PpudAutomationApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var ppudAutomationApiClient: PpudAutomationApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val croNumber = "123456/12A"
    val nomsId = "AB234A"

    ppudAutomationApiMatchResponse(nomsId, croNumber)

    // when
    val actual = ppudAutomationApiClient.search(
      PpudSearchRequest(
        croNumber = croNumber,
        nomsId = nomsId,
        familyName = "Smith",
        dateOfBirth = LocalDate.of(2023, 1, 1),
      ),
    ).block()

    // then
    assertThat(actual.results[0].croNumber, equalTo(croNumber))
  }
}
