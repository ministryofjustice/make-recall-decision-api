package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate
import java.time.LocalDateTime

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
  val terms: List<Term>? = null,
  val sentenceTypeDescription: String? = null,
  val releaseDate: LocalDateTime? = null,
  val releasingPrison: String? = null,
  val licenceExpiryDate: LocalDate? = null,
)

data class Term(
  val years: Int? = null,
  val months: Int? = null,
  val weeks: Int? = null,
  val days: Int? = null,
  val code: String? = null,
)
