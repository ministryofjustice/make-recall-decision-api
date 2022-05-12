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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.LicenceHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse
import java.time.LocalDate
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class LicenceHistoryServiceTest {

  private lateinit var licenceHistoryService: LicenceHistoryService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  val crn = "12345"

  @BeforeEach
  fun setup() {
    licenceHistoryService = LicenceHistoryService(communityApiClient)
  }

  @Test
  fun `given a contact summary and release summary then return these details in the response`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceHistoryService.getLicenceHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(LicenceHistoryResponse(expectedContactSummaryResponse(), allReleaseSummariesResponse()), equalTo(response))
    }
  }

  @Test
  fun `given no release summary details then still retrieve contact summary details`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = licenceHistoryService.getLicenceHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(LicenceHistoryResponse(expectedContactSummaryResponse(), null), equalTo(response))
    }
  }

  @Test
  fun `given no contact summary details then still retrieve release summary details`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceHistoryService.getLicenceHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(LicenceHistoryResponse(emptyList(), allReleaseSummariesResponse()), equalTo(response))
    }
  }

  @Test
  fun `given no contact summary details and no release summary details then still return an empty response`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = licenceHistoryService.getLicenceHistory(crn)

      then(communityApiClient).should().getContactSummary(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(LicenceHistoryResponse(emptyList(), null), equalTo(response))
    }
  }

  private fun expectedContactSummaryResponse(): List<ContactSummaryResponse> {
    return listOf(
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-06-03T07:00Z"),
        descriptionType = "Registration Review",
        outcome = null,
        notes = "Comment added by John Smith on 05/05/2022",
        enforcementAction = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-10T10:39Z"),
        descriptionType = "Police Liaison",
        outcome = "Test - Not Clean / Not Acceptable / Unsuitable",
        notes = "This is a test",
        enforcementAction = "Enforcement Letter Requested"
      )
    )
  }

  private fun allContactSummariesResponse(): ContactSummaryResponseCommunity {
    return ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review"),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison"),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
        )
      )
    )
  }

  private fun allReleaseSummariesResponse(): ReleaseSummaryResponse {

    return ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"))
    )
  }
}
