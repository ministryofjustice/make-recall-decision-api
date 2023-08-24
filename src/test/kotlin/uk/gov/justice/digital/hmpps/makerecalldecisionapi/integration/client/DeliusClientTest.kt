package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RequestFailedException
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
    }.isInstanceOf(RequestFailedException::class.java)
      .hasMessage("Request failed for endpoint: /case-summary/X123456/personal-details after 3 retries")
  }

  @Test
  fun `fetch user details`() {
    userResponse("fred", "test@digital.justice.gov.uk")
    val userDetails = deliusClient.getUserInfo("fred")
    assertThat(userDetails.email).isEqualTo("test@digital.justice.gov.uk")
  }
}
