package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudUpdateOffenderRequest(
  val address: PpudAddress = PpudAddress(),
  val additionalAddresses: List<PpudAddress> = emptyList(),
  val croNumber: String = "",
  val dateOfBirth: LocalDate,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val isInCustody: Boolean,
  val nomsId: String = "",
  val prisonNumber: String,
)
