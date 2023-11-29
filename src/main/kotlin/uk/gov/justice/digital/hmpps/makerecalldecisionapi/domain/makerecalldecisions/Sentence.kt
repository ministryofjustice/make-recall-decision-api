package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class Sentence(
  val bookingId: Int,
  val sentenceSequence: Int? = null,
  val lineSequence: Int? = null,
  val caseSequence: Int? = null,
  val courtDescription: String? = null,
  val sentenceStatus: String? = null,
  val sentenceCategory: String? = null,
  val sentenceCalculationType: String? = null,
  val sentenceTypeDescription: String? = null,
  val sentenceDate: LocalDate,
  val sentenceStartDate: LocalDate? = null,
  val sentenceEndDate: LocalDate? = null,
  val terms: List<Term> = listOf(),
  val offences: List<SentenceOffence> = listOf(),
)

data class Term(
  val years: Int,
  val months: Int,
  val weeks: Int,
  val days: Int,
  val code: String,
)

data class SentenceOffence(
  val offenderChargeId: Int,
  val offenceStartDate: LocalDate,
  val offenceStatute: String,
  val offenceCode: String,
  val offenceDescription: String,
  val indicators: List<String>,
)
