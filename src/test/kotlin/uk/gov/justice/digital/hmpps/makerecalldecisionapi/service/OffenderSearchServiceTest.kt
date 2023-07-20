package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPagedResults
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPeopleRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SearchOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
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
  fun `returns empty list when search by CRN returns no results`() {
    runTest {
      val nonExistentCrn = "X123456"
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = nonExistentCrn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleEmptyResultClientResponse })

      val response = offenderSearch.search(crn = nonExistentCrn)

      assertThat(response.results).isEmpty()
      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns empty list when search by name returns no results`() {
    runTest {
      val nonExistentFirstName = "Laura"
      val nonExistentSurname = "Biding"
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(firstName = nonExistentFirstName, surname = nonExistentSurname)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleEmptyResultClientResponse })

      val response = offenderSearch.search(firstName = nonExistentFirstName, lastName = nonExistentSurname)

      assertThat(response.results).isEmpty()
      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns search results when searching by CRN`() {
    runTest {
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeople1ResultClientResponse })

      val response = offenderSearch.search(crn)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("John Blair")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns search results when searching by name`() {
    runTest {
      val firstName = "John"
      val lastName = "Blair"
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(
          firstName = firstName,
          surname = lastName
        )
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeople1ResultClientResponse })

      val response = offenderSearch.search(firstName = firstName, lastName = lastName)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("John Blair")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `given search result contains case with populated name then do not check user access`() {
    runTest {
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeople1ResultClientResponse })
      lenient().`when`(deliusClient.getUserAccess(username, crn)).doReturn(restrictedAccess())

      offenderSearch.search(crn)

      then(deliusClient).shouldHaveNoInteractions()
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is restricted then set user access fields`() {
    runTest {
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(restrictedAccess())

      val response = offenderSearch.search(crn)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(false)
      assertThat(result.userRestricted).isEqualTo(true)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and user is excluded then set user access fields`() {
    runTest {
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = offenderSearch.search(crn)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(true)
      assertThat(result.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is not restricted then default the name for the case`() {
    runTest {
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val response = offenderSearch.search(crn)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("No name available")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isNull()
      assertThat(result.userExcluded).isFalse
      assertThat(result.userRestricted).isFalse
    }
  }

  @Test
  fun `throws ClientTimeoutException when client throws ClientTimeoutException`() {
    runTest {
      val errorType = "Timeout error"
      val request = OffenderSearchPeopleRequest(
        searchOptions = SearchOptions(crn = crn)
      )
      whenever(offenderSearchApiClient.searchPeople(request)).then {
        throw ClientTimeoutException("Client", errorType)
      }

      try {
        offenderSearch.search(crn)
        fail("No exception was thrown")
      } catch (ex: Throwable) {
        assertThat(ex).isInstanceOf(ClientTimeoutException::class.java)
        assertThat(ex.message).contains(errorType)
      }
    }
  }

  private val searchPeopleEmptyResultClientResponse =
    OffenderSearchPagedResults(
      content = emptyList()
    )

  private val searchPeople1ResultClientResponse =
    OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = "John",
          surname = "Blair",
          dateOfBirth = LocalDate.parse("1982-10-24"),
          otherIds = OtherIds(crn = crn, null, null, null, null)
        )
      )
    )

  private val searchPeopleOmittedDetailsResponse =
    OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = null,
          surname = null,
          dateOfBirth = null,
          otherIds = OtherIds(crn = crn, null, null, null, null)
        )
      )
    )
}
