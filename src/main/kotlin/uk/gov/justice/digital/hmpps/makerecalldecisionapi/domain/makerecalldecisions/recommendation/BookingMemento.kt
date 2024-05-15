package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookingMemento(
  val stage: String,
  val offenderId: String?,
  val sentenceId: String?,
  val releaseId: String?,
  val recallId: String?,
  val failed: Boolean?,
  val failedMessage: String?,
  val uploadedAdditional: List<String>?,
)
