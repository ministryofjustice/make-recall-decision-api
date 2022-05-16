package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderSearchByPhraseRequest
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class OffenderSearchServiceTest {

  private lateinit var offenderSearch: OffenderSearchService

  @Mock
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @BeforeEach
  fun setup() {
    offenderSearch = OffenderSearchService(offenderSearchApiClient)
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

  val offenderSearchResponse = OffenderDetailsResponse(
    content = listOf(
      Content(
        firstName = "John",
        surname = "Blair",
        dateOfBirth = LocalDate.parse("1982-10-24")
      )
    )
  )
}
