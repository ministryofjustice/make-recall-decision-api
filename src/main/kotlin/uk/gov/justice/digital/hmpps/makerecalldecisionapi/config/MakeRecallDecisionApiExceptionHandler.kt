package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoStaffCodeException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationStatusUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import javax.validation.ValidationException

@RestControllerAdvice
class MakeRecallDecisionApiExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(UserAccessException::class)
  fun handleUserAccessException(e: UserAccessException): ResponseEntity<Unit> {
    log.info("UserAccess exception: {}", e.message)
    return ResponseEntity(FORBIDDEN)
  }

  @ExceptionHandler(NoRecommendationFoundException::class)
  fun handleNoRecommendationFoundException(e: NoRecommendationFoundException): ResponseEntity<ErrorResponse> {
    log.info("Recommendation not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No recommendation available: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(InvalidRequestException::class)
  fun handleValidationException(e: InvalidRequestException): ResponseEntity<ErrorResponse> {
    log.info("Invalid request: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Invalid request:: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(PersonNotFoundException::class)
  fun handlePersonNotFoundException(e: PersonNotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Person not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No personal details available: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(DocumentNotFoundException::class)
  fun handleDocumentNotFoundException(e: DocumentNotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Document not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(ClientTimeoutException::class)
  fun handleClientTimeoutException(e: ClientTimeoutException): ResponseEntity<ErrorResponse> {
    log.info("Client timeout exception: {}", e.message)
    return ResponseEntity
      .status(GATEWAY_TIMEOUT)
      .body(
        ErrorResponse(
          status = GATEWAY_TIMEOUT,
          userMessage = "Client timeout: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(NoStaffCodeException::class)
  fun handleNoStaffCodeException(e: NoStaffCodeException): ResponseEntity<ErrorResponse?>? {
    log.error("No staff code found  exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
          error = e.error
        )
      )
  }

  @ExceptionHandler(RecommendationUpdateException::class)
  fun handleRecommendationUpdateException(e: RecommendationUpdateException): ResponseEntity<ErrorResponse?>? {
    log.error("Recommendation update exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
          error = e.error
        )
      )
  }

  @ExceptionHandler(RecommendationStatusUpdateException::class)
  fun handleRecommendationStatusUpdateException(e: RecommendationStatusUpdateException): ResponseEntity<ErrorResponse?>? {
    log.error("Recommendation status update exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
          error = e.error
        )
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
  val error: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
    error: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo, error)
}

open class MakeRecallDecisionException(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause) {
  override fun toString(): String {
    return if (this.message == null) {
      this.javaClass.simpleName
    } else {
      "${this.javaClass.simpleName}: ${this.message}"
    }
  }
}
