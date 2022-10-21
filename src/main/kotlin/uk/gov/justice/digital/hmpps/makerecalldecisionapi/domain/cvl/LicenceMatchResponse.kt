package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl

data class LicenceMatchResponse(
  val licenceId: Int,
  val licenceType: String?,
  val licenceStatus: String?,
  val crn: String?,
)
