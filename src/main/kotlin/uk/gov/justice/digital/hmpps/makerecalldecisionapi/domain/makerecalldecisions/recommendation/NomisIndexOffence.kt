package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate

data class NomisIndexOffence(
  val selected: Int? = null,
  val allOptions: List<OfferedOffence>? = null,
)

data class OfferedOffence(
  val offenderChargeId: Int? = null,
  val offenceCode: String? = null,
  val offenceStatute: String? = null,
  val offenceDescription: String? = null,
  val sentenceDate: LocalDate? = null,
  val courtDescription: String? = null,
  val sentenceStartDate: LocalDate? = null,
  val sentenceEndDate: LocalDate? = null,
  val bookingId: Int? = null,
)
