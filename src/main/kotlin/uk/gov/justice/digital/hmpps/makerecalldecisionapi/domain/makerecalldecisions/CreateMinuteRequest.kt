package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class CreateMinuteRequest(
  val subject: String,
  val text: String,
)
