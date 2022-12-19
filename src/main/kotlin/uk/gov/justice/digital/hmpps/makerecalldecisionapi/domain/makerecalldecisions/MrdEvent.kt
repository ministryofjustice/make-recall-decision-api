package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MrdEvent(
  @JsonProperty("Type")
  val type: String? = "Notification",
  @JsonProperty("MessageId")
  val messageId: String? = UUID.randomUUID().toString(),
  @JsonProperty("Token")
  val token: String? = null,
  @JsonProperty("TopicArn")
  val topicArn: String? = "arn:aws:sns:eu-west-2:000000000000:hmpps-domain",
  @JsonProperty("Message")
  val message: MrdEventMessageBody? = null,
  @JsonProperty("TimeStamp")
  val timeStamp: LocalDateTime? = LocalDateTime.now(),
  @JsonProperty("SignatureVersion")
  val signatureVersion: String? = null,
  @JsonProperty("SubscribeURL")
  val subscribeUrl: String? = null,
  @JsonProperty("Signature")
  val signature: String? = null,
  @JsonProperty("SigningCertURL")
  val signingCertURL: String? = null,
  @JsonProperty("MessageAttributes")
  val messageAttributes: MessageAttributes? = null
)

data class MessageAttributes(
  val eventType: TypeValue? = null,
)

data class MrdEventMessageBody(
  val eventType: String? = null,
  val version: Int? = null,
  val description: String? = null,
  val detailUrl: String? = null, // TODO TBD
  val occurredAt: LocalDateTime? = null,
  val additionalInformation: AdditionalInformation? = null,
  val personReference: PersonReference? = null
)

data class PersonReference(
  val identifiers: List<TypeValue>? = null
)

data class AdditionalInformation(
  val referralId: String? = null,
  val recommendationUrl: String? = null
)

data class TypeValue(
  val type: String? = null,
  val value: String? = null
)
