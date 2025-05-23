package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PersonDetailsControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
) : IntegrationTestBase() {
  @Test
  fun `retrieves person details`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.fullName").isEqualTo("Joe Michael Bloggs")
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.addresses[0].line1").isEqualTo("HMPPS Digital Studio 33 Scotland Street")
        .jsonPath("$.addresses[0].line2").isEqualTo("Sheffield City Centre")
        .jsonPath("$.addresses[0].town").isEqualTo("Sheffield")
        .jsonPath("$.addresses[0].postcode").isEqualTo("S12 345")
        .jsonPath("$.offenderManager.name").isEqualTo("Jane Linda Bloggs")
        .jsonPath("$.offenderManager.phoneNumber").isEqualTo("09056714321")
        .jsonPath("$.offenderManager.email").isEqualTo("first.last@digital.justice.gov.uk")
        .jsonPath("$.offenderManager.probationTeam.code").isEqualTo("C01T04")
        .jsonPath("$.offenderManager.probationTeam.label").isEqualTo("OMU A")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail")
        .isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details")
        .isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("Joe Bloggs")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
    }
  }

  @Test
  fun `handles scenario where no person exists matching crn`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsNotFound(crn)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
        .jsonPath("$.userMessage")
        .isEqualTo("No personal details available: No details available for endpoint: /case-summary/A12345/personal-details")
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessRestricted(crn)
      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(true)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(false)
        .jsonPath("$.userAccessResponse.restrictionMessage")
        .isEqualTo("You are restricted from viewing this offender record. Please contact OM Joe Bloggs")
        .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `gateway timeout 503 given on Delius timeout`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /case-summary/$crn/personal-details endpoint: [No response within 2 seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
