package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.emptyContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class ContactHistoryControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves all contact history details`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true, \"flagSystemGeneratedContacts\": false }"

      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse()
      )
      groupedDocumentsResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")),
              it.set("X-Feature-Flags", featureFlagString)
            )
            )
        }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.contactSummary[0].contactStartDate").isEqualTo("2022-06-03T07:00:00Z")
        .jsonPath("$.contactSummary[0].descriptionType").isEqualTo("Registration Review")
        .jsonPath("$.contactSummary[0].outcome").isEmpty()
        .jsonPath("$.contactSummary[0].sensitive").isEmpty()
        .jsonPath("$.contactSummary[0].notes").isEqualTo("Comment added by John Smith on 05/05/2022")
        .jsonPath("$.contactSummary[0].enforcementAction").isEmpty()
        .jsonPath("$.contactSummary[0].description").isEmpty()
        .jsonPath("$.contactSummary[1].contactStartDate").isEqualTo("2022-05-10T10:39:00Z")
        .jsonPath("$.contactSummary[1].descriptionType").isEqualTo("Police Liaison")
        .jsonPath("$.contactSummary[1].outcome").isEqualTo("Test - Not Clean / Not Acceptable / Unsuitable")
        .jsonPath("$.contactSummary[1].sensitive").isEqualTo(true)
        .jsonPath("$.contactSummary[1].notes").isEqualTo("This is a test")
        .jsonPath("$.contactSummary[1].enforcementAction").isEqualTo("Enforcement Letter Requested")
        .jsonPath("$.contactSummary[1].description").isEqualTo("This is a contact description")
        .jsonPath("$.contactTypeGroups.length()").isEqualTo(2)
        .jsonPath("$.contactTypeGroups[0].groupId").isEqualTo("3")
        .jsonPath("$.contactTypeGroups[0].label").isEqualTo("Appointments")
        .jsonPath("$.contactTypeGroups[0].contactTypeCodes[0]").isEqualTo("COAP")
        .jsonPath("$.contactTypeGroups[1].groupId").isEqualTo("17")
        .jsonPath("$.contactTypeGroups[1].label").isEqualTo("Police")
        .jsonPath("$.contactTypeGroups[1].contactTypeCodes[0]").isEqualTo("C204")
        .jsonPath("$.contactSummary.length()").isEqualTo(3)
        .jsonPath("$.contactSummary[0].contactDocuments.length()").isEqualTo(1)
        .jsonPath("$.contactSummary[1].contactDocuments.length()").isEqualTo(1)
        .jsonPath("$.contactSummary[2].contactDocuments.length()").isEqualTo(1)
        .jsonPath("$.contactSummary[0].contactDocuments[0].id").isEqualTo("f2943b31-2250-41ab-a04d-004e27a97add")
        .jsonPath("$.contactSummary[0].contactDocuments[0].documentName").isEqualTo("test doc.docx")
        .jsonPath("$.contactSummary[0].contactDocuments[0].author").isEqualTo("Trevor Small")
        .jsonPath("$.contactSummary[0].contactDocuments[0].type.code").isEqualTo("CONTACT_DOCUMENT")
        .jsonPath("$.contactSummary[0].contactDocuments[0].type.description").isEqualTo("Contact related document")
        .jsonPath("$.contactSummary[0].contactDocuments[0].extendedDescription").isEqualTo("Contact on 21/06/2022 for Information - from 3rd Party")
        .jsonPath("$.contactSummary[0].contactDocuments[0].lastModifiedAt").isEqualTo("2022-06-21T20:27:23.407")
        .jsonPath("$.contactSummary[0].contactDocuments[0].createdAt").isEqualTo("2022-06-21T20:27:23")
        .jsonPath("$.contactSummary[0].contactDocuments[0].parentPrimaryKeyId").isEqualTo("2504412185")
        .jsonPath("$.contactSummary[1].contactDocuments[0].id").isEqualTo("630ca741-cbb6-4f2e-8e86-73825d8c4d82")
        .jsonPath("$.contactSummary[1].contactDocuments[0].documentName").isEqualTo("a test.pdf")
        .jsonPath("$.contactSummary[1].contactDocuments[0].author").isEqualTo("Jackie Gough")
        .jsonPath("$.contactSummary[1].contactDocuments[0].type.code").isEqualTo("CONTACT_DOCUMENT")
        .jsonPath("$.contactSummary[1].contactDocuments[0].type.description").isEqualTo("Contact related document")
        .jsonPath("$.contactSummary[1].contactDocuments[0].extendedDescription").isEqualTo("Contact on 21/06/2020 for Complementary Therapy Session (NS)")
        .jsonPath("$.contactSummary[1].contactDocuments[0].lastModifiedAt").isEqualTo("2022-06-21T20:29:17.324")
        .jsonPath("$.contactSummary[1].contactDocuments[0].createdAt").isEqualTo("2022-06-21T20:29:17")
        .jsonPath("$.contactSummary[1].contactDocuments[0].parentPrimaryKeyId").isEqualTo("2504435532")
        .jsonPath("$.contactSummary[2].contactDocuments[0].id").isEqualTo("374136ce-f863-48d8-96dc-7581636e461e")
        .jsonPath("$.contactSummary[2].contactDocuments[0].documentName").isEqualTo("ContactDoc.pdf")
        .jsonPath("$.contactSummary[2].contactDocuments[0].author").isEqualTo("Terry Tibbs")
        .jsonPath("$.contactSummary[2].contactDocuments[0].type.code").isEqualTo("CONTACT_DOCUMENT")
        .jsonPath("$.contactSummary[2].contactDocuments[0].type.description").isEqualTo("Contact document")
        .jsonPath("$.contactSummary[2].contactDocuments[0].lastModifiedAt").isEqualTo("2022-06-07T17:00:29.493")
        .jsonPath("$.contactSummary[2].contactDocuments[0].createdAt").isEqualTo("2022-06-07T17:00:29")
        .jsonPath("$.contactSummary[2].contactDocuments[0].parentPrimaryKeyId").isEqualTo("2504435999")
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
    }
  }

  @Test
  fun `given empty contact history then handle response`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, emptyContactSummaryResponse())
      groupedDocumentsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.contactSummary").isArray()
        .jsonPath("$.contactSummary.length()").isEqualTo("0")
        .jsonPath("$.contactTypeGroups.length()").isEqualTo(0)
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
        .jsonPath("$.contactSummary").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on contact summary endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummary = contactSummaryResponse(), delaySeconds = nDeliusTimeout + 2)
      groupedDocumentsResponse(crn)
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - contact summary endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on grouped documents endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummaryResponse())
      groupedDocumentsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - grouped documents endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      userAccessAllowed(crn)
      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
