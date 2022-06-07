package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCustody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.ZonedDateTime

@ActiveProfiles("test")
class ArnApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var arnApiClient: ArnApiClient

  @Test
  fun `retrieves risk summary`() {
    // given
    val crn = "X123456"
    roSHSummaryResponse(crn)

    // and
    val expected = RiskSummaryResponse(
      whoIsAtRisk = "X, Y and Z are at risk",
      natureOfRisk = "The nature of the risk is X",
      riskImminence = "the risk is imminent and more probably in X situation",
      riskIncreaseFactors = "If offender in situation X the risk can be higher",
      riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
      riskInCommunity = RiskInCommunity(
        veryHigh = null,
        high = listOf(
          "Children",
          "Public",
          "Known adult"
        ),
        medium = listOf("Staff"),
        low = listOf("Prisoners")
      ),
      riskInCustody = RiskInCustody(
        veryHigh = listOf(
          "Staff",
          "Prisoners"
        ),
        high = listOf("Known adult"),
        medium = null,
        low = listOf(
          "Children",
          "Public"
        )
      ),
      assessedOn = ZonedDateTime.parse("2022-05-19T08:26:31.349Z[UTC]"),
      overallRiskLevel = "HIGH"
    )

    // when
    val actual = arnApiClient.getRiskSummary(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `throws exception when no person matching crn exists`() {
    val nonExistentCrn = "X123456"
    allOffenderDetailsResponseWithNoOffender(nonExistentCrn)
    assertThatThrownBy {
      runBlockingTest {
        arnApiClient.getRiskSummary(nonExistentCrn).block()
      }
    }.isInstanceOf(PersonNotFoundException::class.java)
      .hasMessage("No details available for crn: $nonExistentCrn")
  }
}
