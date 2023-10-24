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
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
) : IntegrationTestBase() {

  @Test
  fun `retrieves case summary details`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      overviewResponse(crn)
      oasysAssessmentsResponse(crn)
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
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("41")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.activeConvictions.length()").isEqualTo(1)
        .jsonPath("$.activeConvictions[0].additionalOffences.length()").isEqualTo(0)
        .jsonPath("$.activeConvictions[0].mainOffence.description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.activeConvictions[0].sentence.description").isEqualTo("Extended Determinate Sentence")
        .jsonPath("$.activeConvictions[0].sentence.length").isEqualTo("12")
        .jsonPath("$.activeConvictions[0].sentence.lengthUnits").isEqualTo("days")
        .jsonPath("$.activeConvictions[0].sentence.licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(true)
        .jsonPath("$.activeConvictions[0].sentence.custodialStatusCode").isEqualTo("ABC123")
        .jsonPath("$.lastRelease.releaseDate").isEqualTo("2017-09-15")
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
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail")
        .isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.status").isEqualTo("RECALL_CONSIDERED")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details")
        .isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("John Smith")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
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
      personalDetailsResponse(crn)
      overviewResponseNonCustodial(crn)
      deleteAndCreateRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(false)
    }
  }

  @Test
  fun `returns empty offences list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      overviewResponseNoConvictions(crn)

      val result = webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("41")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.activeConvictions.length()").isEqualTo(0)
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
        .jsonPath("$.userAccessResponse.exclusionMessage")
        .isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
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
      overviewResponse(crn)

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
      personalDetailsResponse(crn)
      overviewResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DOCUMENT_DOWNLOADED)

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
  fun `gateway timeout 503 given on Delius timeout`() {
    runTest {
      userAccessAllowed(crn)
      overviewResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /case-summary/$crn/overview endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `given gateway timeout 503 given on ARN API risk management endpoint then set error to TIMEOUT`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      overviewResponse(crn)
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
