package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class RegistrationsResponse(
  val registrations: List<Registration>?
)
fun RegistrationsResponse.toRoshHistory(): RoshHistory {
  return RoshHistory(
    registrations = this.registrations?.filter { it.register?.code == "1" && it.register.description == "RoSH" },
    error = null
  )
}

data class RoshHistory(
  val registrations: List<Registration>? = null,
  val error: String? = null
)
