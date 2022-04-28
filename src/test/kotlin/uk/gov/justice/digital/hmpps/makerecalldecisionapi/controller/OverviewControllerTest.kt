package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Crn

class OverviewControllerTest {
  private val overviewController = OverviewController()

  private val crn = Crn("12345")

  @Test
  fun `returns an overview response successfully`() {
    val response = overviewController.overview(crn)

    assertThat(overviewResponse(), equalTo(response))
  }
}
