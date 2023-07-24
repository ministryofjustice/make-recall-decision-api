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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Pageable
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.PageableResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SearchOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class OffenderSearchServiceTest : ServiceTestBase() {

  private lateinit var offenderSearch: OffenderSearchService

  @Mock
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  private var page = 0

  private var pageSize = 0

  @BeforeEach
  fun setup() {
    offenderSearch = OffenderSearchService(offenderSearchApiClient, userAccessValidator)
    page = Random.Default.nextInt(0, 10)
    pageSize = Random.Default.nextInt(1, 10)
  }

  @Test
  fun `returns empty list when search by CRN returns no results`() {
    runTest {
      val nonExistentCrn = "X123456"
      val request = buildSearchPeopleRequest(crn = nonExistentCrn, page = page, pageSize = pageSize)

      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleEmptyResultClientResponse })

      val response = offenderSearch.search(crn = nonExistentCrn, page = page, pageSize = pageSize)

      assertThat(response.results).isEmpty()
      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns empty list when search by name returns no results`() {
    runTest {
      val nonExistentFirstName = "Laura"
      val nonExistentSurname = "Biding"
      val request = buildSearchPeopleRequest(
        firstName = nonExistentFirstName,
        surname = nonExistentSurname,
        page = page,
        pageSize = pageSize
      )

      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleEmptyResultClientResponse })

      val response = offenderSearch.search(
        firstName = nonExistentFirstName,
        lastName = nonExistentSurname,
        page = page,
        pageSize = pageSize
      )

      assertThat(response.results).isEmpty()
      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns search results when searching by CRN`() {
    runTest {
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { buildSearchPeople1ResultClientResponse(page = page, pageSize = pageSize) })

      val response = offenderSearch.search(crn, page = page, pageSize = pageSize)

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
      val request = buildSearchPeopleRequest(
        firstName = firstName,
        surname = lastName,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { buildSearchPeople1ResultClientResponse(page = page, pageSize = pageSize) })

      val response = offenderSearch.search(firstName = firstName, lastName = lastName, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("John Blair")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(offenderSearchApiClient).should().searchPeople(request)
    }
  }

  @Test
  fun `returns page information when searching`() {
    runTest {
      val totalPages = Random.Default.nextInt(1, 10)
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable {
          buildSearchPeople1ResultClientResponse(
            page = page,
            pageSize = pageSize,
            totalPages = totalPages
          )
        })

      val response = offenderSearch.search(crn, page = page, pageSize = pageSize)

      assertThat(response.paging.page).isEqualTo(page)
      assertThat(response.paging.pageSize).isEqualTo(pageSize)
      assertThat(response.paging.totalNumberOfPages).isEqualTo(totalPages)
    }
  }

  @Test
  fun `given search result contains case with populated name then do not check user access`() {
    runTest {
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { buildSearchPeople1ResultClientResponse(page = page, pageSize = pageSize) })
      lenient().`when`(deliusClient.getUserAccess(username, crn)).doReturn(restrictedAccess())

      offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

      then(deliusClient).shouldHaveNoInteractions()
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is restricted then set user access fields`() {
    runTest {
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(restrictedAccess())

      val response = offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(false)
      assertThat(result.userRestricted).isEqualTo(true)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and user is excluded then set user access fields`() {
    runTest {
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(true)
      assertThat(result.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `given search result contains case with null name and dob fields and access is not restricted then default the name for the case`() {
    runTest {
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      given(offenderSearchApiClient.searchPeople(request))
        .willReturn(Mono.fromCallable { searchPeopleOmittedDetailsResponse })

      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val response = offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

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
      val request = buildSearchPeopleRequest(
        crn = crn,
        page = page,
        pageSize = pageSize
      )
      whenever(offenderSearchApiClient.searchPeople(request)).then {
        throw ClientTimeoutException("Client", errorType)
      }

      try {
        offenderSearch.search(crn = crn, page = page, pageSize = pageSize)
        fail("No exception was thrown")
      } catch (ex: Throwable) {
        assertThat(ex).isInstanceOf(ClientTimeoutException::class.java)
        assertThat(ex.message).contains(errorType)
      }
    }
  }

  private fun buildSearchPeopleRequest(
    firstName: String? = null,
    surname: String? = null,
    crn: String? = null,
    page: Int = 0,
    pageSize: Int = 10
  ): OffenderSearchPeopleRequest {
    val sort = listOf("surname", "firstName", "crn", "offenderId")
    return OffenderSearchPeopleRequest(
      searchOptions = SearchOptions(firstName = firstName, surname = surname, crn = crn),
      pageable = Pageable(page, pageSize, sort = sort)
    )
  }

  private fun buildSearchPeople1ResultClientResponse(page: Int = 0, pageSize: Int = 1, totalPages: Int = page + 1) =
    OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = "John",
          surname = "Blair",
          dateOfBirth = LocalDate.parse("1982-10-24"),
          otherIds = OtherIds(crn = crn, null, null, null, null)
        )
      ),
      pageable = PageableResponse(pageNumber = page, pageSize = pageSize),
      totalPages = totalPages
    )

  private val searchPeopleEmptyResultClientResponse =
    OffenderSearchPagedResults(
      content = emptyList(),
      pageable = PageableResponse(pageNumber = page, pageSize = pageSize),
      totalPages = 0
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
      ),
      pageable = PageableResponse(pageNumber = page, pageSize = pageSize),
      totalPages = 1
    )
}
