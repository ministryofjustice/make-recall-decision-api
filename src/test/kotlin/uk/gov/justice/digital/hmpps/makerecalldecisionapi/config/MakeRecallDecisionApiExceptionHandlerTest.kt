package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import ch.qos.logback.classic.Level
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentTemplateNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PpudValidationException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

class MakeRecallDecisionApiExceptionHandlerTest {

  private val exceptionHandler = MakeRecallDecisionApiExceptionHandler()

  private val logAppender = findLogAppender(MakeRecallDecisionApiExceptionHandler::class.java)

  @Test
  fun handleNotFoundException() {
    val exceptionMessage = randomString()
    val responseEntity = exceptionHandler.handleNotFoundException(NotFoundException(exceptionMessage))

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(responseEntity.body.status).isEqualTo(404)
    assertThat(responseEntity.body.userMessage).isEqualTo("No response found: $exceptionMessage")
    assertInfoMessageWasLogged("Not found exception: $exceptionMessage")
  }

  @Test
  fun handlePpudValidationException() {
    val responseEntity = exceptionHandler.handlePpudValidationException(
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

  @Test
  fun handleAccessDeniedException() {
    val responseEntity = exceptionHandler.handleAccessDeniedException(
      org.springframework.security.access.AccessDeniedException(
        "test error",
      ),
    )

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    assertThat(responseEntity.body.status).isEqualTo(403)
    assertThat(responseEntity.body.userMessage).isEqualTo("Access denied")
    assertThat(responseEntity.body.developerMessage).isEqualTo("test error")
  }

  @Test
  fun handlesDocumentTemplateNotFoundException() {
    val exceptionMessage = randomString()
    val responseEntity = exceptionHandler.handleDocumentTemplateNotFoundException(
      DocumentTemplateNotFoundException(exceptionMessage),
    )

    assertThat(responseEntity).isNotNull
    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(responseEntity.body.status).isEqualTo(INTERNAL_SERVER_ERROR.code())
    assertThat(responseEntity.body.userMessage).isEqualTo("Document template not found")
    assertThat(responseEntity.body.developerMessage).isEqualTo(exceptionMessage)
    assertErrorMessageWasLogged("Document template not found: $exceptionMessage")
  }

  private fun assertInfoMessageWasLogged(message: String) {
    assertMessageWasLogged(message, Level.INFO)
  }

  private fun assertErrorMessageWasLogged(message: String) {
    assertMessageWasLogged(message, Level.ERROR)
  }

  private fun assertMessageWasLogged(errorMessage: String, level: Level) {
    assertThat(logAppender.list).hasSize(1)
    with(logAppender.list[0]) {
      assertThat(level).isEqualTo(level)
      assertThat(formattedMessage).isEqualTo(errorMessage)
    }
  }
}
