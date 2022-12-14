package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CaseOverviewControllerTest(
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  val staffCode = "STFFCDEU"

  @Test
  fun `retrieves case summary details`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      oasysAssessmentsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.RECALL_CONSIDERED)
      riskManagementPlanResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].offences.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].offences[0].mainOffence").isEqualTo("true")
        .jsonPath("$.convictions[0].offences[0].description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.convictions[0].sentenceDescription").isEqualTo("Extended Determinate Sentence")
        .jsonPath("$.convictions[0].sentenceOriginalLength").isEqualTo("12")
        .jsonPath("$.convictions[0].sentenceOriginalLengthUnits").isEqualTo("days")
        .jsonPath("$.convictions[0].licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.convictions[0].sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(true)
        .jsonPath("$.convictions[0].statusCode").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.risk.flags.length()").isEqualTo(1)
        .jsonPath("$.risk.flags[0]").isEqualTo("Victim contact")
        .jsonPath("$.risk.riskManagementPlan.assessmentStatusComplete").isEqualTo(true)
        .jsonPath("$.risk.riskManagementPlan.latestDateCompleted").isEqualTo("2022-10-07T14:20:27.000Z")
        .jsonPath("$.risk.riskManagementPlan.initiationDate").isEqualTo("2022-10-02T14:20:27.000Z")
        .jsonPath("$.risk.riskManagementPlan.lastUpdatedDate").isEqualTo("2022-10-01T14:20:27.000Z")
        .jsonPath("$.risk.riskManagementPlan.contingencyPlans").isEqualTo("I am the contingency plan text")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail").isEqualTo("I have concerns around their behaviour")
        .jsonPath("$.activeRecommendation.status").isEqualTo("RECALL_CONSIDERED")
        .jsonPath("$.risk.assessments.lastUpdatedDate").isEqualTo("2022-04-24T15:00:08.000Z")
        .jsonPath("$.risk.assessments.offenceDataFromLatestCompleteAssessment").isEqualTo(true)
        .jsonPath("$.risk.assessments.offencesMatch").isEqualTo(true)
        .jsonPath("$.risk.assessments.offenceDescription").isEqualTo("Juicy offence details.")
        .jsonPath("$.risk.assessments.error").isEqualTo(null)
    }
  }

  @Test
  fun `sets isCustodial flag to false for case with non custodial conviction`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      nonCustodialConvictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)
      deleteAndCreateRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(false)
    }
  }

  @Test
  fun `returns empty offences list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noActiveConvictionResponse(crn)
      registrationsResponse()
      releaseSummaryResponse(crn)

      val result = webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.convictions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `given user not found then only return user access details`() {
    runTest {
      userNotFound(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userNotFound").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `given case has no recommendation in database then do not return any active recommendation`() {
    runTest {
      // Delete recommendation from database if one was created in previous tests
      deleteRecommendation()
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeRecommendation").isEmpty
    }
  }

  @Test
  fun `given case has recommendation in non-draft state in database then do not return any active recommendation`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DOCUMENT_CREATED)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeRecommendation").isEmpty
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on convictions endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
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
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode, delaySeconds = nDeliusTimeout + 2)
      registrationsResponse()
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
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
  fun `gateway timeout 503 given on Community Api timeout on registrations endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse(delaySeconds = nDeliusTimeout + 2)
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - registrations endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on release summary endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - release summary endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `given gateway timeout 503 given on ARN API risk management endpoint then set error to TIMEOUT`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn)
      riskManagementPlanResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.risk.riskManagementPlan.error").isEqualTo("TIMEOUT")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      webTestClient.get()
        .uri("/cases/$crn/overview")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
