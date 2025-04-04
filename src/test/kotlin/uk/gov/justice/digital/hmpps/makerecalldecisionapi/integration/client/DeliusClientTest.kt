package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RegionNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class DeliusClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var deliusClient: DeliusClient

  @Test
  fun `find by crn`() {
    findByCrnSuccess(surname = "Smith")
    val response = deliusClient.findByCrn("X123456")
    assertThat(response?.name?.surname).isEqualTo("Smith")
  }

  @Test
  fun `find by crn returns null when not found`() {
    val response = deliusClient.findByCrn("X123456")
    assertThat(response).isNull()
  }

  @Test
  fun `find by name`() {
    findByNameSuccess(firstName = "Joe", surname = "Bloggs")
    val response = deliusClient.findByName("Joe", "Bloggs", 0, 1)
    assertThat(response.content.size).isEqualTo(1)
    assertThat(response.content[0].identifiers.crn).isEqualTo("Y654321")
  }

  @Test
  fun `find by name with null forename`() {
    findByNameSuccess(firstName = null, surname = "Bloggs")
    val response = deliusClient.findByName(null, "Bloggs", 0, 1)
    assertThat(response.content.size).isEqualTo(1)
    assertThat(response.content[0].identifiers.crn).isEqualTo("Y654321")
  }

  @Test
  fun `find by name with null surname`() {
    findByNameSuccess(firstName = "Joe", surname = null)
    val response = deliusClient.findByName("Joe", null, 0, 1)
    assertThat(response.content.size).isEqualTo(1)
    assertThat(response.content[0].identifiers.crn).isEqualTo("Y654321")
  }

  @Test
  fun `find by name returns no results`() {
    findByNameNoResults(firstName = "Joe", surname = "Smith")
    val response = deliusClient.findByName("Joe", "Smith", 0, 1)
    assertThat(response.content.size).isEqualTo(0)
  }

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
  fun `retries on http error`() {
    personalDetailsSuccessAfterRetry("X123456")
    val personalDetails = deliusClient.getPersonalDetails("X123456")
    assertThat(personalDetails.personalDetails.name.surname).isEqualTo("Smith")
  }

  @Test
  fun `fetch user details`() {
    userResponse("fred", "test@digital.justice.gov.uk")
    val userDetails = deliusClient.getUserInfo("fred")
    assertThat(userDetails.email).isEqualTo("test@digital.justice.gov.uk")
  }

  @Test
  fun `fetch provider details`() {
    val code = "A11"
    val name = "Provider Name"
    providerByCodeResponse(code, name)
    val provider = deliusClient.getProvider(code)
    assertThat(provider.code).isEqualTo(code)
    assertThat(provider.name).isEqualTo(name)
  }

  @Test
  fun `given unrecognised region code when getProvider is called then RegionNotFoundException is thrown`() {
    val code = "Unrecognised"
    providerNotFoundResponse(code)
    assertThatThrownBy {
      deliusClient.getProvider(code)
    }.isInstanceOf(RegionNotFoundException::class.java)
      .hasMessage("No details available for endpoint: /provider/Unrecognised")
  }
}
