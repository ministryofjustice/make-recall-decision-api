package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MessageAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.util.stream.Collectors

@Profile("default")
@Service
class MrdEventsEmitter(
  hmppsQueueService: HmppsQueueService,
  objectMapper: ObjectMapper,
  customTelemetryClient: TelemetryClient,
) {
  private lateinit var domainEventTopicSnsClient: SnsAsyncClient
  private lateinit var topicArn: String
  private lateinit var objectMapper: ObjectMapper
  private lateinit var telemetryClient: TelemetryClient

  init {
    val domainEventTopic: HmppsTopic? = hmppsQueueService.findByTopicId("hmpps-domain-events")
    topicArn = domainEventTopic!!.arn
    domainEventTopicSnsClient = domainEventTopic.snsClient
    this.objectMapper = objectMapper
    this.telemetryClient = customTelemetryClient
  }

  private fun MessageAttributes.forSns(): Map<String, MessageAttributeValue> {
    val eventType = MessageAttributeValue.builder()
      .dataType("String")
      .stringValue(eventType?.value)
      .build()
    return mapOf("eventType" to eventType)
  }

  fun sendDomainEvent(payload: MrdEvent) {
    try {
      log.info("arn of hmpps-domain-events:: $topicArn")
      val message = objectMapper.writeValueAsString(payload.message)
      val request = PublishRequest.builder()
        .targetArn(topicArn)
        .message(message)
        .messageAttributes(payload.messageAttributes?.forSns())
        .build()
      domainEventTopicSnsClient.publish(request)
      telemetryClient.trackEvent(payload.message?.eventType, asTelemetryMap(payload), null)
    } catch (e: JsonProcessingException) {
      log.error("Failed to convert payload {} to json", payload)
    }
  }

  fun sendAppInsightsEvent(payload: Map<String?, String?>, eventType: String) {
    try {
      log.info("sending App Insights event for deleted recommendation:: ${payload["recommendationId"]}")
      telemetryClient.trackEvent(eventType, payload, null)
    } catch (e: JsonProcessingException) {
      log.error("Failed to convert payload {} to json", payload)
    }
  }

  private fun asTelemetryMap(event: MrdEvent): Map<String, String> {
    val entries = objectMapper
      .convertValue<Map<String, Any>>(event)
      .entries
    return entries.stream().collect(
      Collectors.toMap(
        { (key): Map.Entry<String, Any?> -> key },
        { (_, value): Map.Entry<String, Any?> -> value.toString() },
      ),
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(MrdEventsEmitter::class.java)
  }
}
