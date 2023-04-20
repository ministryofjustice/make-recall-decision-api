package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class OffenderSearchServiceTest : ServiceTestBase() {

  private lateinit var offenderSearch: OffenderSearchService

  @Mock
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @BeforeEach
  fun setup() {
    offenderSearch = OffenderSearchService(offenderSearchApiClient, userAccessValidator)
  }

  @Test
  fun `returns empty list when search returns no results`() {
    runTest {
      val nonExistentCrn = "X123456"
      val request = OffenderSearchByPhraseRequest(
        crn = "X123456"
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.empty())

      val results = offenderSearch.search(nonExistentCrn)

      assertThat(results).isEmpty()
      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  @Test
  fun `returns search results`() {
    runTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        crn = crn
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { offenderSearchResponse })

      val results = offenderSearch.search(crn)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("John Blair")
      assertThat(results[0].crn).isEqualTo(crn)
      assertThat(results[0].dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  @Test
  fun `returns search results for name`() {
    runTest {
      val firstName = "John"
      val lastName = "Doe"
      val request = OffenderSearchByPhraseRequest(
        firstName = firstName,
        surname = lastName
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { offenderSearchResponse })

      val results = offenderSearch.search(firstName = firstName, lastName = lastName)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("John Blair")
      assertThat(results[0].crn).isEqualTo("X12345")
      assertThat(results[0].dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is restricted then set user access fields`() {
    runTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        crn = "X12345"
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { omittedDetailsResponse })

      given(deliusClient.getUserAccess(username, "X12345")).willReturn(restrictedAccess())

      val results = offenderSearch.search(crn)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("null null")
      assertThat(results[0].crn).isEqualTo("X12345")
      assertThat(results[0].dateOfBirth).isNull()
      assertThat(results[0].userExcluded).isEqualTo(false)
      assertThat(results[0].userRestricted).isEqualTo(true)

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is not restricted then default the name for the case`() {
    runTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        crn = crn
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { omittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val results = offenderSearch.search(crn)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("No name available")
      assertThat(results[0].crn).isEqualTo(crn)
      assertThat(results[0].dateOfBirth).isNull()
      assertThat(results[0].userExcluded).isFalse
      assertThat(results[0].userRestricted).isFalse

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  private val offenderSearchResponse = listOf(
    OffenderDetails(
      firstName = "John",
      surname = "Blair",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      otherIds = OtherIds(crn = "X12345", null, null, null, null)
    )
  )

  private val omittedDetailsResponse = listOf(
    OffenderDetails(
      firstName = null,
      surname = null,
      dateOfBirth = null,
      otherIds = OtherIds(crn = "X12345", null, null, null, null)
    )
  )
}
