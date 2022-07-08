package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.release.releaseSummaryDeliusResponse

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class LicenceConditionsControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {
  val staffCode = "STFFCDEU"
  val crn = "A12345"
  val convictionId = 2500614567

  @Test
  fun `retrieves licence condition details`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)
      groupedDocumentsResponse(crn)
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
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
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRelease.notes").isEqualTo("I am a note")
        .jsonPath("$.releaseSummary.lastRelease.reason.code").isEqualTo("ADL")
        .jsonPath("$.releaseSummary.lastRelease.reason.description").isEqualTo("Adult Licence")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.releaseSummary.lastRecall.notes").isEqualTo("I am a second note")
        .jsonPath("$.releaseSummary.lastRecall.reason.code").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRecall.reason.description").isEqualTo("another reason description")
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
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(3)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
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
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRelease.notes").isEqualTo("I am a note")
        .jsonPath("$.releaseSummary.lastRelease.reason.code").isEqualTo("ADL")
        .jsonPath("$.releaseSummary.lastRelease.reason.description").isEqualTo("Adult Licence")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.releaseSummary.lastRecall.notes").isEqualTo("I am a second note")
        .jsonPath("$.releaseSummary.lastRecall.reason.code").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRecall.reason.description").isEqualTo("another reason description")
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
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.convictions[1].convictionId").isEqualTo(convictionId2)
        .jsonPath("$.convictions[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.convictions[0].licenceDocuments.length()").isEqualTo(2)
        .jsonPath("$.convictions[1].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[1].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[1].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId2")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[1].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRelease.notes").isEqualTo("I am a note")
        .jsonPath("$.releaseSummary.lastRelease.reason.code").isEqualTo("ADL")
        .jsonPath("$.releaseSummary.lastRelease.reason.description").isEqualTo("Adult Licence")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.releaseSummary.lastRecall.notes").isEqualTo("I am a second note")
        .jsonPath("$.releaseSummary.lastRecall.reason.code").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRecall.reason.description").isEqualTo("another reason description")
    }
  }

  @Test
  fun `returns empty allLicenceConditions list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noActiveConvictionResponse(crn)
      groupedDocumentsResponse(crn)
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(0)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRelease.notes").isEqualTo("I am a note")
        .jsonPath("$.releaseSummary.lastRelease.reason.code").isEqualTo("ADL")
        .jsonPath("$.releaseSummary.lastRelease.reason.description").isEqualTo("Adult Licence")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.releaseSummary.lastRecall.notes").isEqualTo("I am a second note")
        .jsonPath("$.releaseSummary.lastRecall.reason.code").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRecall.reason.description").isEqualTo("another reason description")
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
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(0)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRelease.notes").isEqualTo("I am a note")
        .jsonPath("$.releaseSummary.lastRelease.reason.code").isEqualTo("ADL")
        .jsonPath("$.releaseSummary.lastRelease.reason.description").isEqualTo("Adult Licence")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.releaseSummary.lastRecall.notes").isEqualTo("I am a second note")
        .jsonPath("$.releaseSummary.lastRecall.reason.code").isEqualTo("ABC123")
        .jsonPath("$.releaseSummary.lastRecall.reason.description").isEqualTo("another reason description")
    }
  }

  @Test
  fun `given no custody release response 400 error then handle licence condition response`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      licenceConditionsResponse(crn, convictionId)
      groupedDocumentsResponse(crn)
      releaseSummaryResponseWithStatusCode(
        crn,
        releaseSummaryDeliusResponse(),
        400
      )

      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].licenceConditions.length()").isEqualTo(2)
        .jsonPath("$.convictions[0].convictionId").isEqualTo(convictionId)
        .jsonPath("$.convictions[0].licenceConditions[0].active").isEqualTo("true")
        .jsonPath("$.convictions[0].licenceConditions[0].startDate").isEqualTo("2022-05-18")
        .jsonPath("$.convictions[0].licenceConditions[0].createdDateTime").isEqualTo("2022-05-18T19:33:56")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.code").isEqualTo("NLC8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeMainCat.description").isEqualTo("Freedom of movement for conviction $convictionId")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.code").isEqualTo("NSTT8")
        .jsonPath("$.convictions[0].licenceConditions[0].licenceConditionTypeSubCat.description").isEqualTo("To only attend places of worship which have been previously agreed with your supervising officer.")
        .jsonPath("$.releaseSummary.lastRelease").isEmpty()
        .jsonPath("$.releaseSummary.lastRecall").isEmpty()
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
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      convictionResponse(crn, staffCode)
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
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode, delaySeconds = nDeliusTimeout + 2)
      groupedDocumentsResponse(crn)
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
    runTest {
      val crn = "A12345"
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
      releaseSummaryResponse(crn)

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
  fun `access denied when insufficient privileges used`() {
    runTest {
      val crn = "X123456"
      webTestClient.get()
        .uri("/cases/$crn/licence-conditions")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
