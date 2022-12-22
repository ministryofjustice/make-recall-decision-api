package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.microsoft.applicationinsights.TelemetryClient
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.util.stream.Collectors

@Profile("default")
@Service
class MrdEventsEmitter(
  hmppsQueueService: HmppsQueueService,
  objectMapper: ObjectMapper,
  customTelemetryClient: TelemetryClient
) {
  private val domainEventTopicSnsClient: AmazonSNSAsync
  private val topicArn: String
  private val objectMapper: ObjectMapper
  private val telemetryClient: TelemetryClient

  init {
    val domainEventTopic: HmppsTopic? = hmppsQueueService.findByTopicId("hmpps-domain-events")
    topicArn = domainEventTopic!!.arn
    domainEventTopicSnsClient = domainEventTopic.snsClient as AmazonSNSAsync
    this.objectMapper = objectMapper
    this.objectMapper.registerModule(JavaTimeModule())
    this.telemetryClient = customTelemetryClient
  }

  fun sendEvent(payload: MrdEvent) {
    try {
      log.info("arn of hmpps-domain-events:: $topicArn")
      val payloadAsJson = JSONObject(payload)
      val messageFromPayload = payloadAsJson.get("Message")
      val payloadWithMessageAsString = payloadAsJson.put("Message", messageFromPayload.toString())
      domainEventTopicSnsClient.publishAsync(
        PublishRequest(topicArn, payloadWithMessageAsString.toString())
      )
      telemetryClient.trackEvent(payload.message?.eventType, asTelemetryMap(payload), null)
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
        { (_, value): Map.Entry<String, Any?> -> value.toString() }
      )
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(MrdEventsEmitter::class.java)
  }
}
