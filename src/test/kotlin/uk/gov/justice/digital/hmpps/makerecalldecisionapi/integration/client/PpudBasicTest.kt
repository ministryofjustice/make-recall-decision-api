package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.micrometer.core.instrument.Counter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient

class PpudBasicTest {
  @Test
  fun `test timeout exception`() {

    val counter = mock(Counter::class.java)
    try {

      PpudAutomationApiClient(
        mock(WebClient::class.java),
        0,
        counter,
        ObjectMapper(),
      ).handleTimeoutException(java.util.concurrent.TimeoutException())


      fail("expected failed exception")
    } catch (e: Throwable) {
      assertThat(e.message, equalTo("PPUD Automation API Client: [No response within 0 seconds]"))

      verify(counter).increment()
    }
  }
}