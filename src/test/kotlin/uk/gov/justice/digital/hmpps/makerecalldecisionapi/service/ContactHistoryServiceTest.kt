package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import java.time.LocalDate
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class ContactHistoryServiceTest : ServiceTestBase() {

  private lateinit var contactHistoryService: ContactHistoryService

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient)
    contactHistoryService = ContactHistoryService(communityApiClient, personDetailsService)

    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
  }

  @Test
  fun `given a contact summary and release summary then return these details in the response`() {
    runBlockingTest {

      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = contactHistoryService.getContactHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), expectedContactSummaryResponse(), expectedContactTypeGroupsResponse(), allReleaseSummariesResponse())))
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runBlockingTest {

      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(true, false) })

      val response = contactHistoryService.getContactHistory(crn)

      then(communityApiClient).should().getUserAccess(crn)

      assertThat(response, equalTo(ContactHistoryResponse(userAccessResponse(true, false), null, null, null, null)))
    }
  }

  @Test
  fun `given no release summary details then still retrieve contact summary details`() {
    runBlockingTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = contactHistoryService.getContactHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), expectedContactSummaryResponse(), expectedContactTypeGroupsResponse(), null)))
    }
  }

  @Test
  fun `given no contact summary details then still retrieve release summary details`() {
    runBlockingTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = contactHistoryService.getContactHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), emptyList(), emptyList(), allReleaseSummariesResponse())))
    }
  }

  @Test
  fun `given no contact summary details and no release summary details then still return an empty response`() {
    runBlockingTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = contactHistoryService.getContactHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(ContactHistoryResponse(null, expectedPersonDetailsResponse(), emptyList(), emptyList(), null)))
    }
  }

  private fun expectedPersonDetailsResponse(): PersonDetails {
    val dateOfBirth = LocalDate.parse("1982-10-24")

    return PersonDetails(
      name = "John Smith",
      dateOfBirth = dateOfBirth,
      age = dateOfBirth?.until(LocalDate.now())?.years,
      gender = "Male",
      crn = "12345"
    )
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
        sensitive = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-10T10:39Z"),
        descriptionType = "Police Liaison",
        outcome = "Test - Not Clean / Not Acceptable / Unsuitable",
        notes = "This is a test",
        enforcementAction = "Enforcement Letter Requested",
        systemGenerated = true,
        code = "COAI",
        sensitive = true
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-12T10:39Z"),
        descriptionType = "Planned visit",
        outcome = "Planned test",
        notes = "This is a test",
        enforcementAction = null,
        systemGenerated = true,
        code = "COAP",
        sensitive = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-11T10:39Z"),
        descriptionType = "Home visit",
        outcome = "Testing",
        notes = "This is another test",
        enforcementAction = null,
        systemGenerated = true,
        code = "CHVS",
        sensitive = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-13T10:39Z"),
        descriptionType = "I am unknown",
        outcome = "Unknown contact",
        notes = "This is an unknown test",
        enforcementAction = null,
        systemGenerated = true,
        code = "ABCD",
        sensitive = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-13T10:39Z"),
        descriptionType = "I am also unknown",
        outcome = "Another unknown contact",
        notes = "This is another unknown test",
        enforcementAction = null,
        systemGenerated = true,
        code = "EFGH",
        sensitive = null
      )
    )
  }

  private fun expectedContactTypeGroupsResponse(): List<ContactGroupResponse> {
    return listOf(
      ContactGroupResponse(
        groupId = "1",
        label = "Appointment",
        contactTypeCodes = listOf("COAI", "COAP")
      ),
      ContactGroupResponse(
        groupId = "2",
        label = "Home Visit",
        contactTypeCodes = listOf("CHVS")
      ),
      ContactGroupResponse(
        groupId = "unknown",
        label = "Unknown",
        contactTypeCodes = listOf("ABCD", "EFGH")
      )
    )
  }

  private fun allContactSummariesResponse(): ContactSummaryResponseCommunity {
    return ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = false, code = "COAI", nationalStandard = false, appointment = false),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
          sensitive = null
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = true, code = "COAI", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
          sensitive = true
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-12T10:39Z"),
          type = ContactType(description = "Planned visit", systemGenerated = true, code = "COAP", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Planned test"),
          notes = "This is a test",
          enforcement = null,
          sensitive = null
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-11T10:39Z"),
          type = ContactType(description = "Home visit", systemGenerated = true, code = "CHVS", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Testing"),
          notes = "This is another test",
          enforcement = null,
          sensitive = null
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-13T10:39Z"),
          type = ContactType(description = "I am unknown", systemGenerated = true, code = "ABCD", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Unknown contact"),
          notes = "This is an unknown test",
          enforcement = null,
          sensitive = null
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-13T10:39Z"),
          type = ContactType(description = "I am also unknown", systemGenerated = true, code = "EFGH", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Another unknown contact"),
          notes = "This is another unknown test",
          enforcement = null,
          sensitive = null
        ),
      )
    )
  }
}
