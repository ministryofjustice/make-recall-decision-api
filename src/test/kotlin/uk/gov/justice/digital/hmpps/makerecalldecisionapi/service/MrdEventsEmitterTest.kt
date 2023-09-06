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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.IdentifierTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MessageAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEventMessageBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonReference
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.TypeValue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic

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
      .isEqualTo("{\"eventType\":\"prison-recall.recommendation.started\",\"version\":1,\"description\":\"Recommendation started (recall or no recall)\",\"detailUrl\":null,\"occurredAt\":\"2022-12-1T14:25:40.117Z\",\"additionalInformation\":{\"referralId\":null,\"recommendationUrl\":\"someurl/cases/crn/overview\",\"contactOutcome\":null,\"bookedBy\":null},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"crn\"}]}}")
  }

  @Test
  fun `will add telemetry event`() {
    service.sendEvent(testPayloadDntrDownload())

    verify(customTelemetryClient).trackEvent(
      ArgumentMatchers.eq("DNTR_LETTER_DOWNLOADED"),
      telemetryAttributesCaptor.capture(),
      ArgumentMatchers.isNull(),
    )
    assertThat(telemetryAttributesCaptor.value).containsAllEntriesOf(
      java.util.Map.of(
        "Message",
        "{eventType=DNTR_LETTER_DOWNLOADED, version=1, description=DNTR letter downloaded, detailUrl=http://someurl, occurredAt=2022-04-26T20:39:47.778, additionalInformation={referralId=null, recommendationUrl=null, contactOutcome=null, bookedBy=null}, personReference={identifiers=[{type=some type, value=some value}]}}",
        "SubscribeUrl",
        "http://localhost:9999",
        "MessageAttributes",
        "{eventType={Type=, Value=}}",
        "Type",
        "Notification",
        "TopicArn",
        "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
        "SigningCertURL",
        "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem",
        "TimeStamp",
        "2022-04-26T20:39:47.778",
        "MessageId",
        "6584074d-6c22-426b-a1cf-cbc472080d99",
      ),
    )
  }

  private fun testPayload(): MrdEvent {
    return MrdEvent(
      messageId = "b4745442-3be4-4e06-8fc6-d8dd8cea87e2",
      timeStamp = "2022-12-1T14:25:40.117Z",
      message = MrdEventMessageBody(
        eventType = "prison-recall.recommendation.started",
        version = 1,
        description = "Recommendation started (recall or no recall)",
        occurredAt = "2022-12-1T14:25:40.117Z",
        personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = "crn"))),
        additionalInformation = AdditionalInformation(
          recommendationUrl = "someurl/cases/crn/overview",
          bookedBy = null,
          contactOutcome = null,
        ),
      ),
      messageAttributes = MessageAttributes(
        eventType = TypeValue(
          type = "String",
          value = "prison-recall.recommendation.started",
        ),
      ),
    )
  }

  private fun testPayloadDntrDownload(): MrdEvent {
    return MrdEvent(
      type = "Notification",
      messageId = "6584074d-6c22-426b-a1cf-cbc472080d99",
      token = null,
      topicArn = "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
      timeStamp = "2022-04-26T20:39:47.778",
      signatureVersion = null,
      subscribeUrl = "http://localhost:9999",
      signature = null,
      signingCertURL = "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-0000000000000000000000.pem",
      messageAttributes = MessageAttributes(eventType = TypeValue(type = "", value = "")),
      message = MrdEventMessageBody(
        eventType = "DNTR_LETTER_DOWNLOADED",
        version = 1,
        description = "DNTR letter downloaded",
        occurredAt = "2022-04-26T20:39:47.778",
        personReference = PersonReference(listOf(IdentifierTypeValue(type = "some type", value = "some value"))),
        additionalInformation = AdditionalInformation(referralId = null, contactOutcome = null, bookedBy = null),
        detailUrl = "http://someurl",
      ),
    )
  }
}
