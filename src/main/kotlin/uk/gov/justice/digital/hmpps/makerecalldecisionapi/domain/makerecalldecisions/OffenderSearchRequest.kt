package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class OffenderSearchRequest(
  val crn: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
)
