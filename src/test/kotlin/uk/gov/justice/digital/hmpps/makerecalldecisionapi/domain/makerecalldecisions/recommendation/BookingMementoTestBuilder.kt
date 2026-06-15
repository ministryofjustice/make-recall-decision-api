package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun bookingMemento(
  stage: String = randomString(),
  offenderId: String? = randomString(),
  sentenceId: String? = randomString(),
  releaseId: String? = randomString(),
  recallId: String? = randomString(),
  failed: Boolean? = randomBoolean(),
  failedMessage: String? = randomString(),
  uploaded: List<String>? = listOf(randomString()),
) = BookingMemento(
  stage = stage,
  offenderId = offenderId,
  sentenceId = sentenceId,
  releaseId = releaseId,
  recallId = recallId,
  failed = failed,
  failedMessage = failedMessage,
  uploaded = uploaded,
)
