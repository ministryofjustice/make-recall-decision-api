package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Crn

class RiskControllerTest {
  private val riskController = RiskController()

  private val crn = Crn("12345")

  @Test
  fun `returns a risk response successfully`() {
    val response = riskController.risk(crn)

    assertThat(riskResponse(), equalTo(response))
  }
}
