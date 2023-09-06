package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class RegistrationsResponse(
  val registrations: List<Registration>?,
)

data class RoshHistory(
  val registrations: List<Registration>? = null,
  val error: String? = null,
)
