package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class OffenderSearchServiceTest : ServiceTestBase() {

  private lateinit var offenderSearch: OffenderSearchService

  @Mock
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @BeforeEach
  fun setup() {
    offenderSearch = OffenderSearchService(offenderSearchApiClient, communityApiClient)
  }

  @Test
  fun `returns empty list when search returns no results`() {
    runBlockingTest {
      val nonExistentCrn = "this person doesn't exist"
      val request = OffenderSearchByPhraseRequest(
        phrase = nonExistentCrn
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
    runBlockingTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        phrase = crn
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
  fun `given search result contains case with null name and dob fields and access is restricted then default to empty string`() {
    runBlockingTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        phrase = crn
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { omittedDetailsResponse })

      given(communityApiClient.getUserAccess(crn))
        .willReturn(Mono.fromCallable { userAccessResponse(false, true) })

      val results = offenderSearch.search(crn)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("Limited access")
      assertThat(results[0].crn).isEqualTo(crn)
      assertThat(results[0].dateOfBirth).isNull()

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is not restricted then find the name for the case`() {
    runBlockingTest {
      val crn = "X12345"
      val request = OffenderSearchByPhraseRequest(
        phrase = crn
      )
      given(offenderSearchApiClient.searchOffenderByPhrase(request))
        .willReturn(Mono.fromCallable { omittedDetailsResponse })

      given(communityApiClient.getUserAccess(crn))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })

      given(communityApiClient.getAllOffenderDetails(ArgumentMatchers.anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })

      val results = offenderSearch.search(crn)

      assertThat(results.size).isEqualTo(1)
      assertThat(results[0].name).isEqualTo("John Smith")
      assertThat(results[0].crn).isEqualTo(crn)
      assertThat(results[0].dateOfBirth).isNull()

      then(offenderSearchApiClient).should().searchOffenderByPhrase(request)
    }
  }

  private val offenderSearchResponse = OffenderDetailsResponse(
    content = listOf(
      OffenderDetails(
        firstName = "John",
        surname = "Blair",
        dateOfBirth = LocalDate.parse("1982-10-24")
      )
    )
  )

  private val omittedDetailsResponse = OffenderDetailsResponse(
    content = listOf(
      OffenderDetails(
        firstName = null,
        surname = null,
        dateOfBirth = null
      )
    )
  )
}
