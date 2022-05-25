package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class CurrentAddress(
  val line1: String?,
  val line2: String?,
  val town: String?,
  val postcode: String?
)
