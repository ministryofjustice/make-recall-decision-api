package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonInclude
import org.json.JSONPropertyName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.utcNowDateTimeString
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MrdEvent(
  @get:JSONPropertyName("Type")
  val type: String? = "Notification",
  @get:JSONPropertyName("MessageId")
  val messageId: String? = UUID.randomUUID().toString(),
  @get:JSONPropertyName("Token")
  val token: String? = null,
  @get:JSONPropertyName("TopicArn")
  val topicArn: String? = "arn: aws:sns:eu-west-2:000000000000:hmpps-domain",
  @get:JSONPropertyName("Message")
  val message: MrdEventMessageBody? = null,
  @get:JSONPropertyName("TimeStamp")
  val timeStamp: String? = utcNowDateTimeString(),
  @get:JSONPropertyName("SignatureVersion")
  val signatureVersion: String? = null,
  @get:JSONPropertyName("SubscribeUrl")
  val subscribeUrl: String? = null,
  @get:JSONPropertyName("Signature")
  val signature: String? = null,
  @get:JSONPropertyName("SigningCertURL")
  val signingCertURL: String? = null,
  @get:JSONPropertyName("MessageAttributes")
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
  val occurredAt: String? = utcNowDateTimeString(),
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
  @get:JSONPropertyName("Type")
  val type: String? = null,
  @get:JSONPropertyName("Value")
  val value: String? = null
)
