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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.CasePage
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Name
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class OffenderSearchServiceTest : ServiceTestBase() {

  private lateinit var offenderSearch: OffenderSearchService

  private var page = 0
  private var pageSize = 0

  @BeforeEach
  fun setup() {
    offenderSearch = OffenderSearchService(deliusClient, userAccessValidator)
    page = Random.Default.nextInt(1, 10)
    pageSize = Random.Default.nextInt(1, 10)
  }

  @Test
  fun `returns empty list when search by CRN returns no results`() {
    runTest {
      val nonExistentCrn = "X123456"

      given(deliusClient.findByCrn(crn = nonExistentCrn)).willReturn(null)

      val response = offenderSearch.search(crn = nonExistentCrn, page = page, pageSize = pageSize)

      assertThat(response.results).isEmpty()
      then(deliusClient).should().findByCrn(crn = nonExistentCrn)
    }
  }

  @Test
  fun `returns empty list when search by name returns no results`() {
    runTest {
      val nonExistentFirstName = "Jane"
      val nonExistentSurname = "Bloggs"
      given(
        deliusClient.findByName(
          firstName = nonExistentFirstName,
          surname = nonExistentSurname,
          page = page - 1,
          pageSize = pageSize,
        ),
      ).willReturn(CasePage(emptyList(), PagedModel.PageMetadata(0, 0, 0, 0)))

      val response = offenderSearch.search(
        firstName = nonExistentFirstName,
        lastName = nonExistentSurname,
        page = page,
        pageSize = pageSize,
      )

      assertThat(response.results).isEmpty()
      then(deliusClient).should().findByName(
        firstName = nonExistentFirstName,
        surname = nonExistentSurname,
        page = page - 1,
        pageSize = pageSize,
      )
    }
  }

  @Test
  fun `returns search results when searching by CRN`() {
    runTest {
      given(deliusClient.findByCrn(crn = crn))
        .willReturn(buildSearchPeople1ResultClientResponse(page = page - 1, pageSize = pageSize).content[0])
      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val response = offenderSearch.search(crn, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("Joe Bloggs")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(deliusClient).should().findByCrn(crn = crn)
    }
  }

  @Test
  fun `returns search results when searching by name`() {
    runTest {
      val firstName = "Joe"
      val lastName = "Bloggs"
      given(
        deliusClient.findByName(
          firstName = firstName,
          surname = lastName,
          page = page - 1,
          pageSize = pageSize,
        ),
      ).willReturn(buildSearchPeople1ResultClientResponse(page = page - 1, pageSize = pageSize))
      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val response = offenderSearch.search(firstName = firstName, lastName = lastName, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.name).isEqualTo("Joe Bloggs")
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))

      then(deliusClient).should().findByName(
        firstName = firstName,
        surname = lastName,
        page = page - 1,
        pageSize = pageSize,
      )
    }
  }

  @Test
  fun `returns page information when searching`() {
    runTest {
      val totalPages = Random.Default.nextInt(1, 10)
      val firstName = "Joe"
      val lastName = "Bloggs"
      given(
        deliusClient.findByName(
          firstName = firstName,
          surname = lastName,
          page = page - 1,
          pageSize = pageSize,
        ),
      ).willReturn(
        buildSearchPeople1ResultClientResponse(
          page = page - 1,
          pageSize = pageSize,
          totalPages = totalPages,
        ),
      )
      given(deliusClient.getUserAccess(username, crn)).willReturn(noAccessLimitations())

      val response = offenderSearch.search(firstName = firstName, lastName = lastName, page = page, pageSize = pageSize)

      assertThat(response.paging.page).isEqualTo(page)
      assertThat(response.paging.pageSize).isEqualTo(pageSize)
      assertThat(response.paging.totalNumberOfPages).isEqualTo(totalPages)
    }
  }

  @Test
  fun `given access is restricted then set user access fields`() {
    runTest {
      given(deliusClient.findByCrn(crn = crn))
        .willReturn(buildSearchPeople1ResultClientResponse().content[0])

      given(deliusClient.getUserAccess(username, crn)).willReturn(restrictedAccess())

      val response = offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(false)
      assertThat(result.userRestricted).isEqualTo(true)
    }
  }

  @Test
  fun `given user is excluded then set user access fields`() {
    runTest {
      given(deliusClient.findByCrn(crn = crn))
        .willReturn(buildSearchPeople1ResultClientResponse().content[0])

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = offenderSearch.search(crn = crn, page = page, pageSize = pageSize)

      assertThat(response.results.size).isEqualTo(1)
      val result = response.results.first()
      assertThat(result.userExcluded).isEqualTo(true)
      assertThat(result.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `throws ClientTimeoutException when client throws ClientTimeoutException`() {
    runTest {
      val errorType = "Timeout error"
      whenever(deliusClient.findByCrn(crn = crn)).then {
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

  private fun buildSearchPeople1ResultClientResponse(page: Int = 0, pageSize: Int = 1, totalPages: Int = page + 1) = CasePage(
    content = listOf(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Joe",
          middleName = null,
          surname = "Bloggs",
        ),
        dateOfBirth = LocalDate.parse("1982-10-24"),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(crn = crn, null, null, null, null),
        gender = "Male",
        ethnicity = null,
        primaryLanguage = null,
      ),
    ),
    page = PagedModel.PageMetadata(pageSize.toLong(), page.toLong(), 1, totalPages.toLong()),
  )
}
