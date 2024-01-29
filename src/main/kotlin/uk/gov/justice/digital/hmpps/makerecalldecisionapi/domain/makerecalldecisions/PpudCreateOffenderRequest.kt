package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudCreateOffender(
  val address: PpudAddress = PpudAddress(),
  val additionalAddresses: List<PpudAddress> = emptyList(),
  val croNumber: String = "",
  val custodyType: String,
  val dateOfBirth: LocalDate,
  val dateOfSentence: LocalDate,
  val ethnicity: String,
  val firstNames: String,
  val familyName: String,
  val gender: String,
  val indexOffence: String,
  val isInCustody: Boolean,
  val mappaLevel: String,
  val nomsId: String = "",
  val prisonNumber: String,
)

data class PpudAddress(
  val premises: String = "",
  val line1: String = "",
  val line2: String = "",
  val postcode: String = "",
  val phoneNumber: String = "",
)
