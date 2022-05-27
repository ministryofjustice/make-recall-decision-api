package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceWithLicenceConditions

data class LicenceConditionsResponse(
  val personalDetailsOverview: PersonDetails? = null,
  val offences: List<OffenceWithLicenceConditions>? = null,
)
