package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

class PpudUpdateOffenderRequest(
  val address: PpudAddress = PpudAddress(),
  val additionalAddresses: List<PpudAddress> = emptyList(),
  croNumber: String? = null,
  val dateOfBirth: LocalDate,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val isInCustody: Boolean,
  nomsId: String? = null,
  val prisonNumber: String,
) {
  val croNumber = croNumber ?: ""
  val nomsId = nomsId ?: ""
}
