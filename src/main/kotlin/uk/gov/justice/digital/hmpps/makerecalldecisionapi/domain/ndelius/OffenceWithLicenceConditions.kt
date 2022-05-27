package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenceWithLicenceConditions(
  val convictionId: Long?,
  val licenceConditions: List<LicenceCondition>?
)
