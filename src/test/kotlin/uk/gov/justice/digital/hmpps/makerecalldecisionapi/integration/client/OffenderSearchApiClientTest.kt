package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPagedResults
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.PageableResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate
import kotlin.random.Random

@ActiveProfiles("test")
class OffenderSearchApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @Test
  fun `retrieves offender details by crn`() {
    // given
    val crn = "X123456"
    val page = Random.Default.nextInt(0, 5)
    val pageSize = Random.Default.nextInt(1, 10)
    offenderSearchByCrnResponse(crn = crn, pageNumber = page, pageSize = pageSize)

    // and
    val expected = OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = "Pontius",
          surname = "Pilate",
          dateOfBirth = LocalDate.parse("2000-11-09"),
          otherIds = OtherIds(crn, null, null, null, null),
        ),
      ),
      pageable = PageableResponse(pageNumber = page, pageSize = pageSize),
      totalPages = 1,
    )

    // when
    val actual = offenderSearchApiClient.searchPeople(crn = crn, page = page, pageSize = pageSize)
      .block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves offender details by name`() {
    // given
    val firstName = "Joe"
    val surname = "Bloggs"
    val dateOfBirth = "2000-11-09"
    val page = Random.Default.nextInt(0, 5)
    val pageSize = Random.Default.nextInt(1, 10)
    offenderSearchByNameResponse(
      crn = crn,
      firstName = firstName,
      surname = surname,
      dateOfBirth = dateOfBirth,
      pageNumber = page,
      pageSize = pageSize,
    )

    // and
    val expected = OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = firstName,
          surname = surname,
          dateOfBirth = LocalDate.parse(dateOfBirth),
          otherIds = OtherIds(crn, null, null, null, null),
        ),
      ),
      pageable = PageableResponse(pageNumber = page, pageSize = pageSize),
      totalPages = 1,
    )

    // when
    val actual =
      offenderSearchApiClient.searchPeople(firstName = firstName, surname = surname, page = page, pageSize = pageSize)
        .block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves offender details by crn when practitioner is excluded from viewing the case`() {
    // given
    val crn = "A123456"
    val page = 0
    val pageSize = 1
    limitedAccessPractitionerOffenderSearchResponse(crn)

    // and
    val expected = OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = null,
          surname = null,
          dateOfBirth = null,
          otherIds = OtherIds(crn, null, null, null, null),
        ),
      ),
      pageable = PageableResponse(
        pageNumber = page,
        pageSize = pageSize,
      ),
      totalPages = 1,
    )

    // when
    val actual = offenderSearchApiClient.searchPeople(crn = crn, page = page, pageSize = pageSize).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
