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
  val staffCode = "STFFCDEU"

  @Test
  fun `retrieves licence condition details for case with custodial conviction`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)
      groupedDocumentsResponse(crn)
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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.convictions[0].sentenceDescription").isEqualTo("Extended Determinate Sentence")
        .jsonPath("$.convictions[0].sentenceOriginalLength").isEqualTo("12")
        .jsonPath("$.convictions[0].sentenceOriginalLengthUnits").isEqualTo("days")
        .jsonPath("$.convictions[0].sentenceStartDate").isEqualTo("2022-04-26")
        .jsonPath("$.convictions[0].licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.convictions[0].sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.convictions[0].postSentenceSupervisionEndDate").isEqualTo("2020-06-27")
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(true)
        .jsonPath("$.convictions[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.convictions[0].licenceDocuments.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].licenceDocuments[0].id").isEqualTo("374136ce-f863-48d8-96dc-7581636e461e")
        .jsonPath("$.convictions[0].licenceDocuments[0].documentName").isEqualTo("GKlicencejune2022.pdf")
        .jsonPath("$.convictions[0].licenceDocuments[0].author").isEqualTo("Tom Thumb")
        .jsonPath("$.convictions[0].licenceDocuments[0].type.code").isEqualTo("CONVICTION_DOCUMENT")
        .jsonPath("$.convictions[0].licenceDocuments[0].type.description").isEqualTo("Sentence related")
        .jsonPath("$.convictions[0].licenceDocuments[0].lastModifiedAt").isEqualTo("2022-06-07T17:00:29.493")
        .jsonPath("$.convictions[0].licenceDocuments[0].createdAt").isEqualTo("2022-06-07T17:00:29")
        .jsonPath("$.convictions[0].licenceDocuments[0].parentPrimaryKeyId").isEqualTo("2500614567")
        .jsonPath("$.convictions[0].licenceDocuments[1].id").isEqualTo("374136ce-f863-48d8-96dc-7581636e123e")
        .jsonPath("$.convictions[0].licenceDocuments[1].documentName").isEqualTo("TDlicencejuly2022.pdf")
        .jsonPath("$.convictions[0].licenceDocuments[1].author").isEqualTo("Wendy Rose")
        .jsonPath("$.convictions[0].licenceDocuments[1].type.code").isEqualTo("CONVICTION_DOCUMENT")
        .jsonPath("$.convictions[0].licenceDocuments[1].type.description").isEqualTo("Sentence related")
        .jsonPath("$.convictions[0].licenceDocuments[1].lastModifiedAt").isEqualTo("2022-07-08T10:00:29.493")
        .jsonPath("$.convictions[0].licenceDocuments[1].createdAt").isEqualTo("2022-06-08T10:00:29")
        .jsonPath("$.convictions[0].licenceDocuments[1].parentPrimaryKeyId").isEqualTo("2500614567")
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
      allOffenderDetailsResponse(crn)
      nonCustodialConvictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)
      groupedDocumentsResponse(crn)
      deleteAndCreateRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(false)
    }
  }

  @Test
  fun `retrieves multiple licence condition details`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      multipleLicenceConditionsResponse(crn, convictionId)
      groupedDocumentsResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(3)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.convictions[0].sentenceStartDate").isEqualTo("2022-04-26")
        .jsonPath("$.convictions[0].licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.convictions[0].sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.convictions[0].postSentenceSupervisionEndDate").isEqualTo("2020-06-27")
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(true)
        .jsonPath("$.convictions[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.convictions[0].licenceConditions[1].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[1].startDate").isEqualTo("2022-05-22")
        .jsonPath("$.convictions[0].licenceConditions[1].createdDateTime").isEqualTo("2022-05-22T08:33:56")
        .jsonPath("$.convictions[0].licenceConditions[1].licenceConditionTypeMainCat.code").isEqualTo("NLC9")
        .jsonPath("$.convictions[0].licenceConditions[1].licenceConditionTypeMainCat.description").isEqualTo("Another main condition")
        .jsonPath("$.convictions[0].licenceConditions[1].licenceConditionTypeSubCat.code").isEqualTo("NSTT9")
        .jsonPath("$.convictions[0].licenceConditions[1].licenceConditionTypeSubCat.description").isEqualTo("Do not attend Hull city center after 8pm")
        .jsonPath("$.convictions[0].licenceDocuments.length()").isEqualTo(2)
    }
  }

  @Test
  fun `retrieves licence condition details for multiple active offences`() {
    runTest {
      val convictionId2 = 123456789L
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      multipleConvictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)
      licenceConditionsResponse(crn, convictionId2)
      groupedDocumentsResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].offences[0].description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.convictions[0].offences[0].mainOffence").isEqualTo(true)
        .jsonPath("$.convictions[0].offences[0].code").isEqualTo("789")
        .jsonPath("$.convictions[1].offences[0].description").isEqualTo("Arson")
        .jsonPath("$.convictions[1].offences[0].mainOffence").isEqualTo(true)
        .jsonPath("$.convictions[1].offences[0].code").isEqualTo("123")
        .jsonPath("$.convictions[1].offences[1].description").isEqualTo("Shoplifting")
        .jsonPath("$.convictions[1].offences[1].mainOffence").isEqualTo(false)
        .jsonPath("$.convictions[1].offences[1].code").isEqualTo("456")
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.convictions[1].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].sentenceStartDate").isEqualTo("2022-04-26")
        .jsonPath("$.convictions[0].licenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.convictions[0].sentenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.convictions[0].postSentenceSupervisionEndDate").isEqualTo("2020-06-23")
        .jsonPath("$.convictions[0].isCustodial").isEqualTo(true)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.convictions[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.convictions[0].licenceDocuments.length()").isEqualTo(2)
        .jsonPath("$.convictions[1].convictionId").isEqualTo(convictionId2)
        .jsonPath("$.convictions[1].sentenceStartDate").isEqualTo("2022-04-25")
        .jsonPath("$.convictions[1].licenceExpiryDate").isEqualTo("2020-06-20")
        .jsonPath("$.convictions[1].sentenceExpiryDate").isEqualTo("2020-06-23")
        .jsonPath("$.convictions[1].postSentenceSupervisionEndDate").isEqualTo("2020-06-22")
        .jsonPath("$.convictions[1].isCustodial").isEqualTo(true)
        .jsonPath("$.convictions[1].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[1].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[1].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId2")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
    }
  }

  @Test
  fun `returns empty allLicenceConditions list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noActiveConvictionResponse(crn)
      groupedDocumentsResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `returns empty licence conditions where no active or inactive licence conditions exist`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      noActiveLicenceConditions(crn, convictionId)
      groupedDocumentsResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(0)
    }
  }

  @Test
  fun `retrieves licence condition details from CVL for a case that exists in CVL`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
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
  fun `gateway timeout 503 given on Community Api timeout on convictions endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      convictionResponse(crn, staffCode)

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
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode, delaySeconds = nDeliusTimeout + 2)
      groupedDocumentsResponse(crn)

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
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      groupedDocumentsResponse(crn)
      licenceConditionsResponse(crn, 2500614567, delaySeconds = nDeliusTimeout + 2)

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
  fun `gateway timeout 503 given on Community Api timeout on grouped documents endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, 2500614567)
      groupedDocumentsResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - grouped documents endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on CVL Api timeout on licence match endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
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
      allOffenderDetailsResponse(crn)
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
