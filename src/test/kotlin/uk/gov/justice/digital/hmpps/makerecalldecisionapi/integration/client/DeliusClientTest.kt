package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class DeliusClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var deliusClient: DeliusClient

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
  fun `fetch user details`() {
    userResponse("fred")
    val userDetails = deliusClient.getUserInfo("fred")
    assertThat(userDetails.email).isEqualTo("test@digital.justice.gov.uk")
  }
}
