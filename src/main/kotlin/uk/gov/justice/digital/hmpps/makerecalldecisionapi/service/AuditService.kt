package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.AuthenticationFacade
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant

@Service
class AuditService(
  @Value("\${spring.application.name}")
  private val serviceName: String,
  private val hmppsQueueService: HmppsQueueService,
  // private val telemetryClient: TelemetryClient,
  private val objectMapper: ObjectMapper,
  private val authenticationFacade: AuthenticationFacade
) {
  private val auditQueue by lazy { hmppsQueueService.findByQueueId("audit") as HmppsQueue }
  private val auditSqsClient by lazy { auditQueue.sqsClient }
  private val auditQueueUrl by lazy { auditQueue.queueUrl }

  suspend fun sendMessage(auditType: AuditType, details: String, crn: String) {

    val auditEvent = AuditEvent(
      what = auditType.name,
      who = authenticationFacade.getUsername(),
      service = serviceName,
      details = details
    )

    // val result =
    auditSqsClient.sendMessage(
      SendMessageRequest(
        auditQueueUrl,
        auditEvent.toJson()
      )
    )

    // telemetryClient.trackEvent(
    //   auditEvent.what,
    //   mapOf("messageId" to result.messageId, "crn" to crn),
    //   null
    // )
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}

data class AuditEvent(
  val what: String,
  val `when`: Instant = Instant.now(),
  val who: String,
  val service: String,
  val details: String? = null,
)
enum class AuditType {
  MRD_CASE_OVERVIEW_VIEWED
}
