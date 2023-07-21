package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPagedResults
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPeopleRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Pageable
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SearchOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate

@ActiveProfiles("test")
class OffenderSearchApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @Test
  fun `retrieves offender details`() {
    // given
    val crn = "X123456"
    offenderSearchByCrnResponse(crn)

    // and
    val expected = OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = "Pontius",
          surname = "Pilate",
          dateOfBirth = LocalDate.parse("2000-11-09"),
          otherIds = OtherIds(crn, null, null, null, null)
        )
      )
    )

    // when
    val actual = offenderSearchApiClient.searchPeople(
      buildSearchPeopleRequest(crn = crn)
    ).block()

    // then
    assertThat(
      actual, equalTo(expected)
    )
  }

  @Test
  fun `retrieves offender details by crn when practitioner is excluded from viewing the case`() {
    // given
    val crn = "A123456"
    limitedAccessPractitionerOffenderSearchResponse(crn)

    // and
    val expected = OffenderSearchPagedResults(
      content = listOf(
        OffenderDetails(
          firstName = null,
          surname = null,
          dateOfBirth = null,
          otherIds = OtherIds(crn, null, null, null, null)
        )
      )
    )

    // when
    val actual = offenderSearchApiClient.searchPeople(
      buildSearchPeopleRequest(crn = crn)
    ).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  private fun buildSearchPeopleRequest(
    crn: String? = null
  ): OffenderSearchPeopleRequest {
    return OffenderSearchPeopleRequest(
      searchOptions = SearchOptions(crn = crn),
      pageable = Pageable(page = 0, size = 10, sort = emptyList())
    )
  }
}
