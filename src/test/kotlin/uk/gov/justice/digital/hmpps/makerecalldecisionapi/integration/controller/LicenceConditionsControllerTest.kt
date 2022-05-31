package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class LicenceConditionsControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {
  val staffCode = "STFFCDEU"

  @Test
  fun `retrieves licence condition details`() {
    runBlockingTest {
      val crn = "A12345"
      val convictionId = 2500000001
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(1)
        .jsonPath("$.offences[0].licenceConditions.length()").isEqualTo(1)
        .jsonPath("$.offences[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.offences[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.offences[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.offences[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
    }
  }

  @Test
  fun `retrieves multiple licence condition details`() {
    runBlockingTest {
      val crn = "A12345"
      val convictionId = 2500000001
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode)
      multipleLicenceConditionsResponse(crn, convictionId)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(1)
        .jsonPath("$.offences[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.offences[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.offences[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.offences[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.offences[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.offences[0].licenceConditions[1].active").isEqualTo("true")
        .jsonPath("$.offences[0].licenceConditions[1].startDate").isEqualTo("2022-05-22")
        .jsonPath("$.offences[0].licenceConditions[1].createdDateTime").isEqualTo("2022-05-22T08:33:56")
        .jsonPath("$.offences[0].licenceConditions[1].licenceConditionTypeMainCat.code").isEqualTo("NLC9")
        .jsonPath("$.offences[0].licenceConditions[1].licenceConditionTypeMainCat.description").isEqualTo("Another main condition")
        .jsonPath("$.offences[0].licenceConditions[1].licenceConditionTypeSubCat.code").isEqualTo("NSTT9")
        .jsonPath("$.offences[0].licenceConditions[1].licenceConditionTypeSubCat.description").isEqualTo("Do not attend Hull city center after 8pm")
    }
  }

  @Test
  fun `retrieves licence condition details for multiple active offences`() {
    runBlockingTest {
      val crn = "A12345"
      val convictionId1 = 2500000001
      val convictionId2 = 123456789L
      allOffenderDetailsResponse(crn)
      multipleConvictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId1)
      licenceConditionsResponse(crn, convictionId2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(2)
        .jsonPath("$.offences[0].licenceConditions.length()").isEqualTo(1)
        .jsonPath("$.offences[1].licenceConditions.length()").isEqualTo(1)
        .jsonPath("$.offences[0].convictionId").isEqualTo(convictionId1)
        .jsonPath("$.offences[1].convictionId").isEqualTo(convictionId2)
        .jsonPath("$.offences[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.offences[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.offences[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId1")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.offences[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.offences[1].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.offences[1].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.offences[1].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.offences[1].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.offences[1].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId2")
        .jsonPath("$.offences[1].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.offences[1].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
    }
  }

  @Test
  fun `returns empty allLicenceConditions list where where no active convictions exist`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      noActiveConvictionResponse(crn)

      val result = webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(0)
    }
  }

  @Test
  fun `returns empty licence conditions where where no active licence conditions exist`() {
    runBlockingTest {
      val crn = "A12345"
      val convictionId1 = 2500000001
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode)
      noActiveLicenceConditions(crn, convictionId1)

      val result = webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(1)
        .jsonPath("$.offences[0].licenceConditions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on convictions endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      unallocatedConvictionResponse(crn, staffCode)
      registrationsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - all offenders endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on all offenders endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode, delaySeconds = nDeliusTimeout + 2)
      registrationsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - convictions endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on licence conditions endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, 2500000001, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - licenceConditions endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
