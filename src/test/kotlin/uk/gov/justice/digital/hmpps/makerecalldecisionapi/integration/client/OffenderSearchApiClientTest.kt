package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.*
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
//    offenderSearchResponse(crn)

    // and
    val expected = OffenderDetailsResponse(
      name = "Pontius Pilate",
      dateOfBirth = LocalDate.parse("2000-04-26"),
      crn = crn
    )

    // when
    val actual = offenderSearchApiClient.searchOffenderByPhrase(OffenderSearchByPhraseRequest(
      matchAllTerms = false,
      phrase = crn,
      probationAreasFilter = listOf("N01", "N02")
    ))//.block()!![0] //TODO ocnsider non-blocking!!

    // then
    assertThat(actual, equalTo(expected))
  }

}
