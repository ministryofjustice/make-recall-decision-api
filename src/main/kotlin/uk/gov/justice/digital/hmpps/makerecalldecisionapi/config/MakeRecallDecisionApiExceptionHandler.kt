package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutRuntimeException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCompletedRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoDeletedRecommendationRationaleException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoManagementOversightException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PpudValidationException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationStatusUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException

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
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(UserAccessException::class)
  fun handleUserAccessException(e: UserAccessException): ResponseEntity<Unit> {
    log.info("UserAccess exception: {}", e.message)
    return ResponseEntity(FORBIDDEN)
  }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> {
    log.info("No resource found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No resource found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(WebClientResponseException.InternalServerError::class)
  fun handleDownstreamDependencyErrorException(e: WebClientResponseException.InternalServerError): ResponseEntity<ErrorResponse> {
    log.info("Downstream dependency error exception: {}", e.message)
    return ResponseEntity
      .status(BAD_GATEWAY)
      .body(
        ErrorResponse(
          status = BAD_GATEWAY,
          userMessage = "A system on which we depend has failed: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(PpudValidationException::class)
  fun handlePpudValidationException(e: PpudValidationException): ResponseEntity<ErrorResponse> {
    log.info("Ppud validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        e.errorResponse,
      )
  }

  @ExceptionHandler(NoDeletedRecommendationRationaleException::class)
  fun handleNoDeletedRecommendationRationaleException(e: NoDeletedRecommendationRationaleException): ResponseEntity<ErrorResponse> {
    log.info("Deleted recommendation rationale not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No deleted recommendation rationale available: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(NoManagementOversightException::class)
  fun handleNoNoManagementOversightException(e: NoManagementOversightException): ResponseEntity<ErrorResponse> {
    log.info("Management Oversight not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No management oversight available: ${e.message}",
          developerMessage = e.message,
        ),
      )
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
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(NoCompletedRecommendationFoundException::class)
  fun handleNoCompletedRecommendationFoundException(e: NoCompletedRecommendationFoundException): ResponseEntity<ErrorResponse> {
    log.info("No Completed Recommendation found exception: {}", e.message)
    return ResponseEntity
      .status(NO_CONTENT)
      .body(
        ErrorResponse(
          status = NO_CONTENT,
          userMessage = "No completed recommendation available: ${e.message}",
          developerMessage = e.message,
        ),
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
          developerMessage = e.message,
        ),
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
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "No response found: ${e.message}",
          developerMessage = e.message,
        ),
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
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ClientTimeoutException::class)
  fun handleClientTimeoutException(e: ClientTimeoutException): ResponseEntity<ErrorResponse> =
    handleClientTimeoutException(e.message)

  @ExceptionHandler(ClientTimeoutRuntimeException::class)
  fun handleClientTimeoutRuntimeException(e: ClientTimeoutRuntimeException): ResponseEntity<ErrorResponse> =
    handleClientTimeoutException(e.message)

  fun handleClientTimeoutException(message: String?): ResponseEntity<ErrorResponse> {
    log.info("Client timeout exception: {}", message)
    return ResponseEntity
      .status(GATEWAY_TIMEOUT)
      .body(
        ErrorResponse(
          status = GATEWAY_TIMEOUT,
          userMessage = "Client timeout: $message",
          developerMessage = message,
        ),
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
          developerMessage = e.message,
        ),
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
          error = e.error,
        ),
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
          error = e.error,
        ),
      )
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
  fun handleAccessDeniedException(e: org.springframework.security.access.AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.error("Access denied", e)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          userMessage = "Access denied",
          developerMessage = e.message,
        ),
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
  val error: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
    error: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo, error)
}

open class MakeRecallDecisionException(override val message: String? = null, override val cause: Throwable? = null) :
  Exception(message, cause) {
  override fun toString(): String {
    return if (this.message == null) {
      this.javaClass.simpleName
    } else {
      "${this.javaClass.simpleName}: ${this.message}"
    }
  }
}

// It is unclear why MakeRecallDecisionException extends Exception and not RuntimeException. Because
// it is widely used, for now we create this runtime version and can look into this replacing the
// above one with.
// The reason for creating a runtime version is that uses of doOnError for processing react Monos
// should be throwing runtime exceptions. It looks like they got around this back then through the
// GetValueAndHandleWrappedException method, but I couldn't find or think of a reason why we might
// want to do this instead of using a runtime one and letting the relevant @ExceptionHandler-annotated
// method pick it up.
open class MakeRecallDecisionRuntimeException(
  override val message: String? = null,
  override val cause: Throwable? = null,
) :
  RuntimeException(message, cause) {
  override fun toString(): String {
    return if (this.message == null) {
      this.javaClass.simpleName
    } else {
      "${this.javaClass.simpleName}: ${this.message}"
    }
  }
}
