package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException

class MakeRecallDecisionApiExceptionHandlerTest {

  @Test
  fun handleNotFoundException() {
    val responseEntity =
      MakeRecallDecisionApiExceptionHandler().handleNotFoundException(NotFoundException("test error"))

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(responseEntity.body.status).isEqualTo(404)
    assertThat(responseEntity.body.userMessage).isEqualTo("No response found: test error")
  }
}
