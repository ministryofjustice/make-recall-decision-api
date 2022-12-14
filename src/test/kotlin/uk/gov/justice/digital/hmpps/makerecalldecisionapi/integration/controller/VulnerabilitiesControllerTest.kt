package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class VulnerabilitiesControllerTest(
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves risk vulnerability details`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      risksWithFullTextResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/vulnerabilities")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.vulnerabilities.suicide.previous").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.suicide.previousConcernsText").isEqualTo("Previous risk of suicide concerns due to ...")
        .jsonPath("$.vulnerabilities.suicide.current").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.suicide.currentConcernsText").isEqualTo("Risk of suicide concerns due to ...")
        .jsonPath("$.vulnerabilities.selfHarm.previous").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.selfHarm.previousConcernsText").isEqualTo("Previous risk of self harm concerns due to ...")
        .jsonPath("$.vulnerabilities.selfHarm.current").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.selfHarm.currentConcernsText").isEqualTo("Risk of self harm concerns due to ...")
        .jsonPath("$.vulnerabilities.vulnerability.previous").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.vulnerability.previousConcernsText").isEqualTo("Previous risk of vulnerability concerns due to ...")
        .jsonPath("$.vulnerabilities.vulnerability.current").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.vulnerability.currentConcernsText").isEqualTo("Risk of vulnerability concerns due to ...")
        .jsonPath("$.vulnerabilities.custody.previous").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.custody.previousConcernsText").isEqualTo("Previous risk of custody concerns due to ...")
        .jsonPath("$.vulnerabilities.custody.current").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.custody.currentConcernsText").isEqualTo("Risk of custody concerns due to ...")
        .jsonPath("$.vulnerabilities.hostelSetting.previous").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.hostelSetting.previousConcernsText").isEqualTo("Previous risk of hostel setting concerns due to ...")
        .jsonPath("$.vulnerabilities.hostelSetting.current").isEqualTo("Yes")
        .jsonPath("$.vulnerabilities.hostelSetting.currentConcernsText").isEqualTo("Risk of hostel setting concerns due to ...")
        .jsonPath("$.vulnerabilities.lastUpdatedDate").isEqualTo("2022-11-23T00:01:50.000Z")
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail").isEqualTo("I have concerns around their behaviour")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/vulnerabilities")
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
        .uri("/cases/$crn/vulnerabilities")
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
  fun `gateway timeout 503 given on OASYS ARN timeout on risk endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      risksWithFullTextResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/vulnerabilities")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.vulnerabilities.error")
        .isEqualTo("TIMEOUT")
    }
  }
}
