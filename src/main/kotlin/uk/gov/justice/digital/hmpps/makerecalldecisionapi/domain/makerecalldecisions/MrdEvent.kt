package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.utcNowDateTimeString
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MrdEvent(
  @get:JsonProperty("Type")
  val type: String? = "Notification",
  @get:JsonProperty("MessageId")
  val messageId: String? = UUID.randomUUID().toString(),
  @get:JsonProperty("Token")
  val token: String? = null,
  @get:JsonProperty("TopicArn")
  val topicArn: String? = "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
  @get:JsonProperty("Message")
  val message: MrdEventMessageBody? = null,
  @get:JsonProperty("TimeStamp")
  val timeStamp: String? = utcNowDateTimeString(),
  @get:JsonProperty("SignatureVersion")
  val signatureVersion: String? = null,
  @get:JsonProperty("SubscribeUrl")
  val subscribeUrl: String? = null,
  @get:JsonProperty("Signature")
  val signature: String? = null,
  @get:JsonProperty("SigningCertURL")
  val signingCertURL: String? = null,
  @get:JsonProperty("MessageAttributes")
  val messageAttributes: MessageAttributes? = null
)
fun toDntrDownloadedEventPayload(crn: String?): MrdEvent {
  return MrdEvent(
    timeStamp = utcNowDateTimeString(),
    message = MrdEventMessageBody(
      eventType = "DNTR_LETTER_DOWNLOADED",
      version = 1,
      description = "DNTR letter downloaded",
      occurredAt = utcNowDateTimeString(),
      detailUrl = "", // TODO TBD
      personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn)))
    )
  )
}

fun toManagerRecallDecisionMadeEventPayload(recommendationUrl: String?, crn: String?, contactOutcome: String?, username: String, staffCode: String?): MrdEvent {
  return MrdEvent(
    timeStamp = utcNowDateTimeString(),
    message = MrdEventMessageBody(
      eventType = "prison-recall.recommendation.management-oversight",
      version = 1,
      description = "Management Oversight - Recall",
      occurredAt = utcNowDateTimeString(),
      detailUrl = "", // TODO TBD
      personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
      additionalInformation = AdditionalInformation(
        contactOutcome = contactOutcome,
        recommendationUrl = recommendationUrl,
        bookedBy = BookedBy(username, staffCode)
      )
    ),
    messageAttributes = MessageAttributes(eventType = TypeValue(type = "String", value = "prison-recall.recommendation.management-oversight"))
  )
}

fun toRecommendationStartedEventPayload(recommendationUrl: String, crn: String?): MrdEvent {
  return MrdEvent(
    timeStamp = utcNowDateTimeString(),
    message = MrdEventMessageBody(
      eventType = "prison-recall.recommendation.started",
      version = 1,
      description = "Recommendation started (recall or no recall)",
      occurredAt = utcNowDateTimeString(),
      detailUrl = "", // TODO TBD
      personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
      additionalInformation = AdditionalInformation(recommendationUrl = recommendationUrl, bookedBy = null, contactOutcome = null)
    ),
    messageAttributes = MessageAttributes(eventType = TypeValue(type = "String", value = "prison-recall.recommendation.started"))
  )
}

data class MessageAttributes(
  val eventType: TypeValue? = null,
)

data class MrdEventMessageBody(
  val eventType: String? = null,
  val version: Int? = null,
  val description: String? = null,
  val detailUrl: String? = null, // TODO TBD
  val occurredAt: String? = utcNowDateTimeString(),
  val additionalInformation: AdditionalInformation? = null,
  val personReference: PersonReference? = null
)

data class PersonReference(
  val identifiers: List<IdentifierTypeValue>? = null
)

data class AdditionalInformation(
  val referralId: String? = null,
  val recommendationUrl: String? = null,
  val contactOutcome: String?,
  val bookedBy: BookedBy?
)

data class BookedBy(
  val username: String,
  val staffCode: String? = null
)

data class TypeValue(
  @get:JsonProperty("Type")
  val type: String? = null,
  @get:JsonProperty("Value")
  val value: String? = null
)

data class IdentifierTypeValue(
  val type: String? = null,
  val value: String? = null
)
