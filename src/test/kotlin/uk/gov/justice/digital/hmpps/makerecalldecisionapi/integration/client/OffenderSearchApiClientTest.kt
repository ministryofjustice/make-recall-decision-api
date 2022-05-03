package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderSearchByPhraseRequest
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
    unallocatedOffenderSearchResponse(crn)

    // and
    val expected = OffenderDetailsResponse(
      content = listOf(
        Content(
          firstName = "Pontius",
          surname = "Pilate",
          dateOfBirth = LocalDate.parse("2000-11-09")
        )
      )
    )

    // when
    val actual = offenderSearchApiClient.searchOffenderByPhrase(
      OffenderSearchByPhraseRequest(
        matchAllTerms = false,
        phrase = crn,
        probationAreasFilter = listOf("N01", "N02")
      )
    ).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
