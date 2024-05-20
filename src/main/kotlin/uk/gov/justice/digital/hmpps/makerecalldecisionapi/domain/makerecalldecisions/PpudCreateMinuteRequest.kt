package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudCreateMinuteRequest(
  val subject: String,
  val text: String,
)
