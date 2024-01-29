package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PpudValidationException

class MakeRecallDecisionApiExceptionHandlerTest {

  @Test
  fun handleNotFoundException() {
    val responseEntity =
      MakeRecallDecisionApiExceptionHandler().handleNotFoundException(NotFoundException("test error"))

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(responseEntity.body.status).isEqualTo(404)
    assertThat(responseEntity.body.userMessage).isEqualTo("No response found: test error")
  }

  @Test
  fun handlePpudValidationException() {
    val responseEntity =
      MakeRecallDecisionApiExceptionHandler().handlePpudValidationException(
        PpudValidationException(
          ErrorResponse(
            HttpStatus.BAD_REQUEST,
            errorCode = 234,
            userMessage = "some error",
            developerMessage = "even better error",
          ),
        ),
      )

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(responseEntity.body.status).isEqualTo(400)
    assertThat(responseEntity.body.errorCode).isEqualTo(234)
    assertThat(responseEntity.body.userMessage).isEqualTo("some error")
    assertThat(responseEntity.body.developerMessage).isEqualTo("even better error")
  }
}
