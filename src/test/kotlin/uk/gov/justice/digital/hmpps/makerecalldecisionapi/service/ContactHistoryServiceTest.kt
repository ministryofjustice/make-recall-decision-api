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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.Contact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.ContactSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactTypeSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class ContactHistoryServiceTest : ServiceTestBase() {

  private lateinit var contactHistoryService: ContactHistoryService

  @BeforeEach
  fun setup() {
    documentService = DocumentService(communityApiClient, userAccessValidator)
    personDetailsService = PersonDetailsService(deliusClient, userAccessValidator, recommendationService)
    contactHistoryService = ContactHistoryService(deliusClient, userAccessValidator, recommendationService)

    given(deliusClient.getUserAccess(anyString(), anyString()))
      .willReturn(userAccessResponse(false, false, false))
  }

  @Test
  fun `given a contact summary and release summary then return these details in the response`() {
    runTest {

      given(deliusClient.getContactHistory(crn))
        .willReturn(deliusContactHistoryResponse())

      val response = contactHistoryService.getContactHistory(crn)

      then(deliusClient).should().getContactHistory(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), expectedContactSummaryResponse(), expectedContactTypeGroupsResponseWithSystemGeneratedContactsFeatureOn())))
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = contactHistoryService.getContactHistory(crn)

      then(deliusClient).should().getUserAccess(username, crn)

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

      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Forbidden"))

      val response = contactHistoryService.getContactHistory(crn)

      then(deliusClient).should().getUserAccess(username, crn)

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
      given(deliusClient.getContactHistory(crn))
        .willReturn(deliusContactHistoryResponse(contacts = emptyList()))

      val response = contactHistoryService.getContactHistory(crn)

      then(deliusClient).should().getContactHistory(crn)

      assertThat(
        response,
        equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), emptyList(), emptyList()))
      )
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
        sensitive = false,
        contactDocuments = listOf(
          CaseDocument(
            id = "f2943b31-2250-41ab-a04d-004e27a97add",
            documentName = "test doc.docx",
            lastModifiedAt = ZonedDateTime.parse("2022-06-21T20:27:23.407Z"),
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
            lastModifiedAt = ZonedDateTime.parse("2022-06-21T20:29:17.324Z"),
          ),
          CaseDocument(
            id = "630ca741-cbb6-4f2e-8e86-73825d8c4999",
            documentName = "conviction contact doc.pdf",
            lastModifiedAt = ZonedDateTime.parse("2022-06-23T20:29:17.324Z"),
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
        sensitive = false,
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
        sensitive = false,
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
        sensitive = false,
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
        sensitive = false,
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

  private fun deliusContactHistoryResponse(
    contacts: List<Contact> = listOf(
      Contact(
        startDateTime = ZonedDateTime.parse("2022-06-03T07:00Z"),
        type = Contact.Type(
          description = "Registration Review",
          systemGenerated = false,
          code = "COAI",
        ),
        outcome = null,
        notes = "Comment added by John Smith on 05/05/2022",
        enforcementAction = null,
        sensitive = false,
        description = "This is a contact description",
        documents = listOf(
          Contact.DocumentReference(
            id = "f2943b31-2250-41ab-a04d-004e27a97add",
            name = "test doc.docx",
            lastUpdated = ZonedDateTime.parse("2022-06-21T20:27:23.407Z"),
          )
        )
      ),
      Contact(
        startDateTime = ZonedDateTime.parse("2022-05-10T10:39Z"),
        type = Contact.Type(
          description = "Police Liaison",
          systemGenerated = true,
          code = "COAI",
        ),
        outcome = "Test - Not Clean / Not Acceptable / Unsuitable",
        notes = "This is a test",
        enforcementAction = "Enforcement Letter Requested",
        sensitive = true,
        description = null,
        documents = listOf(
          Contact.DocumentReference(
            id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82",
            name = "a test.pdf",
            lastUpdated = ZonedDateTime.parse("2022-06-21T20:29:17.324Z"),
          ),
          Contact.DocumentReference(
            id = "630ca741-cbb6-4f2e-8e86-73825d8c4999",
            name = "conviction contact doc.pdf",
            lastUpdated = ZonedDateTime.parse("2022-06-23T20:29:17.324Z"),
          )
        ),
      ),
      Contact(
        startDateTime = ZonedDateTime.parse("2022-05-12T10:39Z"),
        type = Contact.Type(
          description = "Planned visit",
          systemGenerated = true,
          code = "COAP",
        ),
        outcome = "Planned test",
        notes = "This is a test",
        enforcementAction = null,
        sensitive = false,
        description = null,
        documents = listOf()
      ),
      Contact(
        startDateTime = ZonedDateTime.parse("2022-05-11T10:39Z"),
        type = Contact.Type(
          description = "Home visit",
          systemGenerated = true,
          code = "CHVS",
        ),
        outcome = "Testing",
        notes = "This is another test",
        enforcementAction = null,
        sensitive = false,
        description = null,
        documents = listOf()
      ),
      Contact(
        startDateTime = ZonedDateTime.parse("2022-05-13T10:39Z"),
        type = Contact.Type(
          description = "Unpaid work",
          systemGenerated = true,
          code = "EASU",
        ),
        outcome = "Unknown contact",
        notes = "This is another test",
        enforcementAction = null,
        sensitive = false,
        description = null,
        documents = listOf()
      ),
      Contact(
        startDateTime = ZonedDateTime.parse("2022-05-13T10:39Z"),
        type = Contact.Type(
          description = "I am also unknown",
          systemGenerated = true,
          code = "EFGH",
        ),
        outcome = "Another unknown contact",
        notes = "This is another unknown test",
        enforcementAction = null,
        sensitive = false,
        description = null,
        documents = listOf()
      )
    )
  ) = ContactHistory(
    personalDetails = deliusPersonalDetailsResponse().personalDetails,
    summary = ContactSummary(
      types = if (contacts.isNotEmpty()) listOf(
        ContactTypeSummary(code = "CHVS", description = "Home visit", total = 1),
        ContactTypeSummary(code = "COAI", description = "Initial appointment", total = 2),
        ContactTypeSummary(code = "COAP", description = "Planned visit", total = 1),
        ContactTypeSummary(code = "EASU", description = "Unpaid work", total = 1),
        ContactTypeSummary(code = "EFGH", description = "Unknown", total = 1),
      ) else emptyList(),
      hits = contacts.size,
      total = contacts.size
    ),
    contacts = contacts
  )
}
