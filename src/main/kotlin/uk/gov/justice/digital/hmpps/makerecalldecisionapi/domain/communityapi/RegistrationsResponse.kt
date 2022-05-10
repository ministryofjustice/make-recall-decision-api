package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

data class RegistrationsResponse(
  val registrations: List<Registration>
)

data class Registration(
  val active: Boolean,
  val type: Type
)

data class Type(
  val code: String,
  val description: String
)
