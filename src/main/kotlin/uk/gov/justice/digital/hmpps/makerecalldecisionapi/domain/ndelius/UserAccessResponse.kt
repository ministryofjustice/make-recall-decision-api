package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class UserAccessResponse(
  val userRestricted: Boolean?,
  val userExcluded: Boolean?,
  val exclusionMessage: String?,
  val restrictionMessage: String?
)
