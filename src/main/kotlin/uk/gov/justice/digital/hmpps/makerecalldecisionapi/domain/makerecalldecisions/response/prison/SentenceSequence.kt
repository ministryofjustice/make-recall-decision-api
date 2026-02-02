package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison

import java.time.LocalDate
import java.time.LocalDateTime

data class SentenceSequence(
  val indexSentence: Sentence,
  val sentencesInSequence: MutableMap<Int, List<Sentence>>? = null,
)

// TODO review which fields are actually needed (this was originally based on the Nomis
//      API model, from which we already seemed to be taking more fields than we need)
data class Sentence(
  val bookingId: Int? = null,
  val sentenceSequence: Int? = null,
  val lineSequence: Int? = null,
  val consecutiveToSequence: Int? = null,
  val caseSequence: Int? = null,
  val courtDescription: String? = null,
  val sentenceStatus: String? = null,
  val sentenceCategory: String? = null,
  val sentenceCalculationType: String? = null,
  val sentenceTypeDescription: String? = null,
  val sentenceDate: LocalDate? = null,
  val sentenceStartDate: LocalDate? = null,
  // We don't provide a sentence end date, as NOMIS & Calculate Release Date don't provide an accurate
  // one. Only the expiry/end date for the sequence the sentence is a part of is useful to the users
  val sentenceSequenceExpiryDate: LocalDate? = null,
  val terms: List<Term> = listOf(),
  val offences: List<SentenceOffence> = listOf(),
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

data class SentenceOffence(
  val offenderChargeId: Int? = null,
  val offenceStartDate: LocalDate? = null,
  val offenceStatute: String? = null,
  val offenceCode: String? = null,
  val offenceDescription: String? = null,
  val indicators: List<String>? = null,
)
