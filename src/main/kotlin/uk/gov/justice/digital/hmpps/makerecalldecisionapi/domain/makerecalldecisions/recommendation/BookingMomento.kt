package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookingMomento(
  val stage: String,
  val offenderId: String?,
  val sentenceId: String?,
  val releaseId: String?,
  val failed: Boolean?,
  val failedMessage: String?,
)
