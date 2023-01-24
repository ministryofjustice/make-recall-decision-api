package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class ContactHistoryServiceTest : ServiceTestBase() {

  private lateinit var contactHistoryService: ContactHistoryService

  @BeforeEach
  fun setup() {
    documentService = DocumentService(communityApiClient, userAccessValidator)
    personDetailsService = PersonDetailsService(communityApiClient, userAccessValidator, recommendationService)
    contactHistoryService = ContactHistoryService(communityApiClient, personDetailsService, userAccessValidator, documentService, recommendationService)

    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false, false) })
  }

  @Test
  fun `given a contact summary and release summary then return these details in the response`() {
    runTest {

      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = contactHistoryService.getContactHistory(crn, FeatureFlags(flagShowSystemGenerated = false))

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), expectedContactSummaryResponse(), expectedContactTypeGroupsResponse())))
    }
  }

  @Test
  fun `given a contact summary with feature flag system generated contacts on then return these details in the response`() {
    runTest {

      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = contactHistoryService.getContactHistory(crn, FeatureFlags(flagShowSystemGenerated = true))

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), expectedContactSummaryResponse(), expectedContactTypeGroupsResponseWithSystemGeneratedContactsFeatureOn())))
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = contactHistoryService.getContactHistory(crn, FeatureFlags())

      then(communityApiClient).should().getUserAccess(crn)

      assertThat(
        response,
        equalTo(
          ContactHistoryResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null), null, null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given user not found for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          404, "Forbidden", null, null, null
        )
      )

      val response = contactHistoryService.getContactHistory(crn, FeatureFlags())

      then(communityApiClient).should().getUserAccess(crn)

      assertThat(
        response,
        equalTo(
          ContactHistoryResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null), null, null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given no contact summary details then still retrieve release summary details`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = contactHistoryService.getContactHistory(crn, FeatureFlags())

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), emptyList(), emptyList())))
    }
  }

  private fun expectedContactSummaryResponse(): List<ContactSummaryResponse> {
    return listOf(
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-06-03T07:00Z"),
        descriptionType = "Registration Review",
        outcome = null,
        notes = "Comment added by John Smith on 05/05/2022",
        enforcementAction = null,
        systemGenerated = false,
        code = "COAI",
        sensitive = null,
        contactDocuments = listOf(
          CaseDocument(
            id = "f2943b31-2250-41ab-a04d-004e27a97add",
            documentName = "test doc.docx",
            author = "Trevor Small",
            type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"),
            extendedDescription = "Contact on 21/06/2022 for Information - from 3rd Party",
            lastModifiedAt = "2022-06-21T20:27:23.407",
            createdAt = "2022-06-21T20:27:23",
            parentPrimaryKeyId = 2504763194L
          )
        ),
        description = "This is a contact description"
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-10T10:39Z"),
        descriptionType = "Police Liaison",
        outcome = "Test - Not Clean / Not Acceptable / Unsuitable",
        notes = "This is a test",
        enforcementAction = "Enforcement Letter Requested",
        systemGenerated = true,
        code = "COAI",
        sensitive = true,
        contactDocuments = listOf(
          CaseDocument(
            id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82",
            documentName = "a test.pdf",
            author = "Jackie Gough",
            type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"),
            extendedDescription = "Contact on 21/06/2020 for Complementary Therapy Session (NS)",
            lastModifiedAt = "2022-06-21T20:29:17.324",
            createdAt = "2022-06-21T20:29:17",
            parentPrimaryKeyId = 2504763206L
          ),
          CaseDocument(
            id = "630ca741-cbb6-4f2e-8e86-73825d8c4999",
            documentName = "conviction contact doc.pdf",
            author = "Luke Smith",
            type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related conviction document"),
            extendedDescription = "Contact on 23/06/2020 for Complementary Therapy Session (NS)",
            lastModifiedAt = "2022-06-23T20:29:17.324",
            createdAt = "2022-06-23T20:29:17",
            parentPrimaryKeyId = 2504763206L
          )
        ),
        description = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-12T10:39Z"),
        descriptionType = "Planned visit",
        outcome = "Planned test",
        notes = "This is a test",
        enforcementAction = null,
        systemGenerated = true,
        code = "COAP",
        sensitive = null,
        contactDocuments = emptyList(),
        description = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-11T10:39Z"),
        descriptionType = "Home visit",
        outcome = "Testing",
        notes = "This is another test",
        enforcementAction = null,
        systemGenerated = true,
        code = "CHVS",
        sensitive = null,
        contactDocuments = emptyList(),
        description = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-13T10:39Z"),
        descriptionType = "Unpaid work",
        outcome = "Unknown contact",
        notes = "This is another test",
        enforcementAction = null,
        systemGenerated = true,
        code = "EASU",
        sensitive = null,
        contactDocuments = emptyList(),
        description = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-13T10:39Z"),
        descriptionType = "I am also unknown",
        outcome = "Another unknown contact",
        notes = "This is another unknown test",
        enforcementAction = null,
        systemGenerated = true,
        code = "EFGH",
        sensitive = null,
        contactDocuments = emptyList(),
        description = null
      )
    )
  }

  private fun expectedContactTypeGroupsResponse(): List<ContactGroupResponse> {
    return listOf(
      ContactGroupResponse(
        groupId = "3",
        label = "Appointments",
        contactTypeCodes = listOf("CHVS", "COAI", "COAP")
      ),
      ContactGroupResponse(
        groupId = "11",
        label = "Home visit",
        contactTypeCodes = listOf("CHVS")
      ),
      ContactGroupResponse(
        groupId = "unknown",
        label = "Not categorised",
        contactTypeCodes = listOf("EASU", "EFGH")
      )
    )
  }

  private fun expectedContactTypeGroupsResponseWithSystemGeneratedContactsFeatureOn(): List<ContactGroupResponse> {
    return listOf(
      ContactGroupResponse(
        groupId = "3",
        label = "Appointments",
        contactTypeCodes = listOf("CHVS", "COAI", "COAP")
      ),
      ContactGroupResponse(
        groupId = "11",
        label = "Home visit",
        contactTypeCodes = listOf("CHVS")
      ),
      ContactGroupResponse(
        groupId = "21",
        label = "Unpaid work",
        contactTypeCodes = listOf("EASU")
      ),
      ContactGroupResponse(
        groupId = "unknown",
        label = "Not categorised",
        contactTypeCodes = listOf("EFGH")
      )
    )
  }

  private fun allContactSummariesResponse(): ContactSummaryResponseCommunity {
    return ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactId = 2504763194L,
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = false, code = "COAI", nationalStandard = false, appointment = false),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
          sensitive = null,
          description = "This is a contact description"
        ),
        Content(
          contactId = 2504763206L,
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = true, code = "COAI", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
          sensitive = true,
          description = null
        ),
        Content(
          contactId = 2504763207L,
          contactStart = OffsetDateTime.parse("2022-05-12T10:39Z"),
          type = ContactType(description = "Planned visit", systemGenerated = true, code = "COAP", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Planned test"),
          notes = "This is a test",
          enforcement = null,
          sensitive = null,
          description = null
        ),
        Content(
          contactId = 987L,
          contactStart = OffsetDateTime.parse("2022-05-11T10:39Z"),
          type = ContactType(description = "Home visit", systemGenerated = true, code = "CHVS", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Testing"),
          notes = "This is another test",
          enforcement = null,
          sensitive = null,
          description = null
        ),
        Content(
          contactId = 654L,
          contactStart = OffsetDateTime.parse("2022-05-13T10:39Z"),
          type = ContactType(description = "Unpaid work", systemGenerated = true, code = "EASU", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Unknown contact"),
          notes = "This is another test",
          enforcement = null,
          sensitive = null,
          description = null
        ),
        Content(
          contactId = 321L,
          contactStart = OffsetDateTime.parse("2022-05-13T10:39Z"),
          type = ContactType(description = "I am also unknown", systemGenerated = true, code = "EFGH", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Another unknown contact"),
          notes = "This is another unknown test",
          enforcement = null,
          sensitive = null,
          description = null
        ),
      )
    )
  }
}
