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
  val messageAttributes: MessageAttributes? = null,
)

fun toDntrDownloadedEventPayload(crn: String?): MrdEvent = MrdEvent(
  timeStamp = utcNowDateTimeString(),
  message = MrdEventMessageBody(
    eventType = "DNTR_LETTER_DOWNLOADED",
    version = 1,
    description = "DNTR letter downloaded",
    occurredAt = utcNowDateTimeString(),
    detailUrl = "",
    personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
  ),
)

fun toSystemDeleteRecommendationEventPayload(
  region: String?,
  crn: String?,
  recommendationId: String?,
  username: String?,
): Map<String?, String?> = mapOf(
  "userName" to username,
  "crn" to crn,
  "recommendationId" to recommendationId,
  "region" to region,
)

fun toDeleteRecommendationRationaleDomainEventPayload(
  recommendationUrl: String?,
  crn: String?,
  contactOutcome: String?,
  username: String,
  detailUrl: String,
): MrdEvent = MrdEvent(
  timeStamp = utcNowDateTimeString(),
  message = MrdEventMessageBody(
    eventType = "prison-recall.recommendation.deleted",
    version = 1,
    description = "Deleted recommendation in 'Consider a recall'",
    occurredAt = utcNowDateTimeString(),
    detailUrl = detailUrl,
    personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
    additionalInformation = AdditionalInformation(
      contactOutcome = contactOutcome,
      recommendationUrl = recommendationUrl,
      bookedBy = BookedBy(username),
    ),
  ),
  messageAttributes = MessageAttributes(
    eventType = TypeValue(
      type = "String",
      value = "prison-recall.recommendation.deleted",
    ),
  ),
)

fun toConsiderationRecallEventPayload(
  recommendationUrl: String?,
  crn: String?,
  username: String,
  detailUrl: String,
): MrdEvent = MrdEvent(
  timeStamp = utcNowDateTimeString(),
  message = MrdEventMessageBody(
    eventType = "prison-recall.recommendation.consideration",
    version = 1,
    description = "Management Oversight - Consideration Rationale",
    occurredAt = utcNowDateTimeString(),
    detailUrl = detailUrl,
    personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
    additionalInformation = AdditionalInformation(
      contactOutcome = null,
      recommendationUrl = recommendationUrl,
      bookedBy = BookedBy(username),
    ),
  ),
  messageAttributes = MessageAttributes(
    eventType = TypeValue(
      type = "String",
      value = "prison-recall.recommendation.consideration",
    ),
  ),
)

fun toManagerRecallDecisionMadeEventPayload(
  recommendationUrl: String?,
  crn: String?,
  contactOutcome: String?,
  username: String,
  detailUrl: String,
): MrdEvent = MrdEvent(
  timeStamp = utcNowDateTimeString(),
  message = MrdEventMessageBody(
    eventType = "prison-recall.recommendation.management-oversight",
    version = 1,
    description = "Management Oversight - Recall",
    occurredAt = utcNowDateTimeString(),
    detailUrl = detailUrl,
    personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
    additionalInformation = AdditionalInformation(
      contactOutcome = contactOutcome,
      recommendationUrl = recommendationUrl,
      bookedBy = BookedBy(username),
    ),
  ),
  messageAttributes = MessageAttributes(
    eventType = TypeValue(
      type = "String",
      value = "prison-recall.recommendation.management-oversight",
    ),
  ),
)

fun toRecommendationStartedEventPayload(recommendationUrl: String, crn: String?): MrdEvent = MrdEvent(
  timeStamp = utcNowDateTimeString(),
  message = MrdEventMessageBody(
    eventType = "prison-recall.recommendation.started",
    version = 1,
    description = "Recommendation started (recall or no recall)",
    occurredAt = utcNowDateTimeString(),
    detailUrl = "",
    personReference = PersonReference(listOf(IdentifierTypeValue(type = "CRN", value = crn))),
    additionalInformation = AdditionalInformation(
      recommendationUrl = recommendationUrl,
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

data class MessageAttributes(
  val eventType: TypeValue? = null,
)

data class MrdEventMessageBody(
  val eventType: String? = null,
  val version: Int? = null,
  val description: String? = null,
  val detailUrl: String? = null,
  val occurredAt: String? = utcNowDateTimeString(),
  val additionalInformation: AdditionalInformation? = null,
  val personReference: PersonReference? = null,
)

data class PersonReference(
  val identifiers: List<IdentifierTypeValue>? = null,
)

data class AdditionalInformation(
  val referralId: String? = null,
  val recommendationUrl: String? = null,
  val contactOutcome: String?,
  val bookedBy: BookedBy?,
)

data class BookedBy(
  val username: String,
)

data class TypeValue(
  @get:JsonProperty("Type")
  val type: String? = null,
  @get:JsonProperty("Value")
  val value: String? = null,
)

data class IdentifierTypeValue(
  val type: String? = null,
  val value: String? = null,
)
