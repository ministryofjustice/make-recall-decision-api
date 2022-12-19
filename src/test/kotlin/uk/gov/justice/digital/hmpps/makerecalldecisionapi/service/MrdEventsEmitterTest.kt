package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AdditionalInformation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MessageAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEventMessageBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonReference
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.TypeValue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class MrdEventsEmitterTest {
  private val objectMapper = ObjectMapper()
  private lateinit var service: MrdEventsEmitter

  @Mock
  private lateinit var customTelemetryClient: TelemetryClient

  @Captor
  private lateinit var telemetryAttributesCaptor: ArgumentCaptor<Map<String, String>>

  @Captor
  private lateinit var publishRequestCaptor: ArgumentCaptor<PublishRequest>

  private val hmppsQueueService = mock<HmppsQueueService>()
  private val domainEventSnsClient = mock<AmazonSNSAsync>()

  @BeforeEach
  fun setup() {
    whenever(hmppsQueueService.findByTopicId("hmpps-domain-events"))
      .thenReturn(HmppsTopic("hmpps-domain-events", "topicARN", domainEventSnsClient))

    service = MrdEventsEmitter(hmppsQueueService, objectMapper, customTelemetryClient)
  }

  @Test
  fun `will add payload as message`() {
    service.sendEvent(testPayload())
    verify(domainEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request).extracting("message")
      .isEqualTo("{\"type\":\"Notification\",\"messageId\":\"6584074d-6c22-426b-a1cf-cbc472080d99\",\"topicArn\":\"arn:aws:sns:eu-west-2:000000000000:hmpps-domain\",\"message\":{\"eventType\":\"DNTR_LETTER_DOWNLOADED\",\"version\":1,\"description\":\"DNTR letter downloaded\",\"detailUrl\":\"http://someurl\",\"occurredAt\":[2022,4,26,20,39,47,778000000],\"additionalInformation\":{\"referralId\":null,\"recommendationUrl\":null},\"personReference\":{\"identifiers\":[{\"type\":\"some type\",\"value\":\"some value\"}]}},\"timeStamp\":[2022,4,26,20,39,47,778000000],\"subscribeUrl\":\"http://localhost:9999\",\"signingCertURL\":\"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem\",\"messageAttributes\":{\"eventType\":{\"type\":\"\",\"value\":\"\"}}}")
  }

  @Test
  fun `will add telemetry event`() {
    service.sendEvent(testPayload())

    verify(customTelemetryClient).trackEvent(
      ArgumentMatchers.eq("DNTR_LETTER_DOWNLOADED"),
      telemetryAttributesCaptor.capture(),
      ArgumentMatchers.isNull()
    )
    assertThat(telemetryAttributesCaptor.value).containsAllEntriesOf(
      java.util.Map.of(
        "message",
        "{eventType=DNTR_LETTER_DOWNLOADED, version=1, description=DNTR letter downloaded, detailUrl=http://someurl, occurredAt=[2022, 4, 26, 20, 39, 47, 778000000], additionalInformation={referralId=null, recommendationUrl=null}, personReference={identifiers=[{type=some type, value=some value}]}}",
        "messageAttributes",
        "{eventType={type=, value=}}",
        "messageId",
        "6584074d-6c22-426b-a1cf-cbc472080d99",
        "signingCertURL",
        "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem",
        "subscribeUrl",
        "http://localhost:9999",
        "timeStamp",
        "[2022, 4, 26, 20, 39, 47, 778000000]",
        "topicArn",
        "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
        "type",
        "Notification"
      )
    )
  }

  private fun testPayload(): MrdEvent {
    return MrdEvent(
      type = "Notification",
      messageId = "6584074d-6c22-426b-a1cf-cbc472080d99",
      token = null,
      topicArn = "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
      timeStamp = LocalDateTime.parse("2022-04-26T20:39:47.778"),
      signatureVersion = null,
      subscribeUrl = "http://localhost:9999",
      signature = null,
      signingCertURL = "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem",
      messageAttributes = MessageAttributes(eventType = TypeValue(type = "", value = "")),
      message = MrdEventMessageBody(
        eventType = "DNTR_LETTER_DOWNLOADED",
        version = 1,
        description = "DNTR letter downloaded",
        occurredAt = LocalDateTime.parse("2022-04-26T20:39:47.778"),
        personReference = PersonReference(listOf(TypeValue(type = "some type", value = "some value"))),
        additionalInformation = AdditionalInformation(referralId = null),
        detailUrl = "http://someurl"
      )
    )
  }
}
