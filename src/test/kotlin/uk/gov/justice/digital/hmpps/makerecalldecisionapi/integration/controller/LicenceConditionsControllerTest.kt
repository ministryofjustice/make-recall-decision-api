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
class LicenceConditionsControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Value("\${cvl.client.timeout}") private val cvlTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves licence condition details for case with custodial conviction`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      licenceConditionsResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.activeConvictions.length()").isEqualTo(1)
        .jsonPath("$.activeConvictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.activeConvictions[0].sentence.description").isEqualTo("Extended Determinate Sentence")
        .jsonPath("$.activeConvictions[0].sentence.length").isEqualTo("12")
        .jsonPath("$.activeConvictions[0].sentence.lengthUnits").isEqualTo("days")
        .jsonPath("$.activeConvictions[0].sentence.licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(true)
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.code").isEqualTo("NLC8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.description").isEqualTo("Freedom of movement")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.code").isEqualTo("NSTT8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail").isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details").isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("John Smith")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
    }
  }

  @Test
  fun `sets isCustodial flag to false for case with non custodial conviction`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      nonCustodialLicenceConditionsResponse(crn)
      deleteAndCreateRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(false)
    }
  }

  @Test
  fun `retrieves multiple licence condition details`() {
    runTest {
      userAccessAllowed(crn)
      multipleLicenceConditionsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.activeConvictions.length()").isEqualTo(1)
        .jsonPath("$.activeConvictions[0].licenceConditions.length()").isEqualTo(3)
        .jsonPath("$.activeConvictions[0].sentence.licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(true)
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.code").isEqualTo("NLC8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.description").isEqualTo("Freedom of movement")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.code").isEqualTo("NSTT8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.activeConvictions[0].licenceConditions[1].mainCategory.code").isEqualTo("NLC9")
        .jsonPath("$.activeConvictions[0].licenceConditions[1].mainCategory.description").isEqualTo("Another main condition")
        .jsonPath("$.activeConvictions[0].licenceConditions[1].subCategory.code").isEqualTo("NSTT9")
        .jsonPath("$.activeConvictions[0].licenceConditions[1].subCategory.description").isEqualTo("Do not attend Hull city center after 8pm")
    }
  }

  @Test
  fun `retrieves licence condition details for multiple active offences`() {
    runTest {
      userAccessAllowed(crn)
      licenceConditionsResponseWithMultipleActiveConvictions(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.activeConvictions.length()").isEqualTo(2)
        .jsonPath("$.activeConvictions[0].mainOffence.description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.activeConvictions[0].mainOffence.code").isEqualTo("789")
        .jsonPath("$.activeConvictions[1].mainOffence.description").isEqualTo("Arson")
        .jsonPath("$.activeConvictions[1].mainOffence.code").isEqualTo("123")
        .jsonPath("$.activeConvictions[1].additionalOffences[0].description").isEqualTo("Shoplifting")
        .jsonPath("$.activeConvictions[1].additionalOffences[0].code").isEqualTo("456")
        .jsonPath("$.activeConvictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.activeConvictions[1].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.activeConvictions[0].sentence.licenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.activeConvictions[0].sentence.isCustodial").isEqualTo(true)
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.code").isEqualTo("NLC8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.description").isEqualTo("Freedom of movement")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.code").isEqualTo("NSTT8")
        .jsonPath("$.activeConvictions[0].licenceConditions[0].subCategory.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.activeConvictions[1].sentence.licenceExpiryDate").isEqualTo("2020-06-20")
        .jsonPath("$.activeConvictions[1].sentence.sentenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.activeConvictions[1].sentence.isCustodial").isEqualTo(true)
        .jsonPath("$.activeConvictions[1].licenceConditions[0].mainCategory.code").isEqualTo("NLC8")
        .jsonPath("$.activeConvictions[1].licenceConditions[0].mainCategory.description").isEqualTo("Freedom of movement")
        .jsonPath("$.activeConvictions[1].licenceConditions[0].subCategory.code").isEqualTo("NSTT8")
        .jsonPath("$.activeConvictions[1].licenceConditions[0].subCategory.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
    }
  }

  @Test
  fun `returns empty allLicenceConditions list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      licenceConditionsResponseWithNoActiveConvictions(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
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
        .jsonPath("$.activeConvictions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `returns empty licence conditions where no active or inactive licence conditions exist`() {
    runTest {
      userAccessAllowed(crn)
      noActiveLicenceConditions(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
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
        .jsonPath("$.activeConvictions.length()").isEqualTo(1)
        .jsonPath("$.activeConvictions[0].licenceConditions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `retrieves licence condition details from CVL for a case that exists in CVL`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      cvlLicenceMatchResponse(nomsId, crn)
      cvlLicenceByIdResponse(123344, nomsId, crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions-cvl")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.licenceConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].conditionalReleaseDate").isEqualTo("2022-06-10")
        .jsonPath("$.licenceConditions[0].actualReleaseDate").isEqualTo("2022-06-11")
        .jsonPath("$.licenceConditions[0].sentenceStartDate").isEqualTo("2022-06-12")
        .jsonPath("$.licenceConditions[0].sentenceEndDate").isEqualTo("2022-06-13")
        .jsonPath("$.licenceConditions[0].licenceStartDate").isEqualTo("2022-06-14")
        .jsonPath("$.licenceConditions[0].licenceExpiryDate").isEqualTo("2022-06-15")
        .jsonPath("$.licenceConditions[0].topupSupervisionStartDate").isEqualTo("2022-06-16")
        .jsonPath("$.licenceConditions[0].topupSupervisionExpiryDate").isEqualTo("2022-06-17")
        .jsonPath("$.licenceConditions[0].standardLicenceConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].standardLicenceConditions[0].text").isEqualTo("This is a standard licence condition")
        .jsonPath("$.licenceConditions[0].standardPssConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].standardPssConditions[0].text").isEqualTo("This is a standard PSS licence condition")
        .jsonPath("$.licenceConditions[0].additionalLicenceConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].additionalLicenceConditions[0].text").isEqualTo("This is an additional licence condition")
        .jsonPath("$.licenceConditions[0].additionalLicenceConditions[0].expandedText").isEqualTo("Expanded additional licence condition")
        .jsonPath("$.licenceConditions[0].additionalPssConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].additionalPssConditions[0].text").isEqualTo("This is an additional PSS licence condition")
        .jsonPath("$.licenceConditions[0].additionalPssConditions[0].expandedText").isEqualTo("Expanded additional PSS licence condition")
        .jsonPath("$.licenceConditions[0].bespokeConditions.length()").isEqualTo(1)
        .jsonPath("$.licenceConditions[0].bespokeConditions[0].text").isEqualTo("This is a bespoke condition")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail").isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details").isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("John Smith")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
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
  fun `gateway timeout 503 given on Delius timeout`() {
    runTest {
      userAccessAllowed(crn)
      licenceConditionsResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /case-summary/$crn/licence-conditions endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on CVL Api timeout on licence match endpoint`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      cvlLicenceMatchResponse(nomsId, crn, delaySeconds = cvlTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions-cvl")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: CVL API Client - licence match endpoint: [No response within $cvlTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on CVL Api timeout on get licence id endpoint`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      cvlLicenceMatchResponse(nomsId, crn)
      cvlLicenceByIdResponse(123344, nomsId, crn, delaySeconds = cvlTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions-cvl")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: CVL API Client - licence by id endpoint: [No response within $cvlTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
