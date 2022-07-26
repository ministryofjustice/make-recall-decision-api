package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate

@ActiveProfiles("test")
class OffenderSearchApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var offenderSearchApiClient: OffenderSearchApiClient

  @Test
  fun `retrieves offender details by crn`() {
    // given
    val crn = "X123456"
    offenderSearchResponse(crn)

    // and
    val expected = OffenderDetailsResponse(
      content = listOf(
        OffenderDetails(
          firstName = "Pontius",
          surname = "Pilate",
          dateOfBirth = LocalDate.parse("2000-11-09"),
          otherIds = OtherIds(crn)
        )
      )
    )

    // when
    val actual = offenderSearchApiClient.searchOffenderByPhrase(
      OffenderSearchByPhraseRequest(
        matchAllTerms = false,
        phrase = crn
      )
    ).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves offender details by crn when practitioner is excluded from viewing the case`() {
    // given
    val crn = "X123456"
    limitedAccessPractitionerOffenderSearchResponse(crn)

    // and
    val expected = OffenderDetailsResponse(
      content = listOf(
        OffenderDetails(
          firstName = null,
          surname = null,
          dateOfBirth = null,
          otherIds = OtherIds(crn)
        )
      )
    )

    // when
    val actual = offenderSearchApiClient.searchOffenderByPhrase(
      OffenderSearchByPhraseRequest(
        matchAllTerms = false,
        phrase = crn
      )
    ).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
