package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime

fun sentenceSequence(
  indexSentence: Sentence = sentence(),
  sentencesInSequence: MutableMap<Int, List<Sentence>>? = mutableMapOf(randomInt() to mutableListOf(sentence())),
) = SentenceSequence(
  indexSentence = indexSentence,
  sentencesInSequence = sentencesInSequence,
)

fun sentence(
  bookingId: Int? = randomInt(),
  sentenceSequence: Int? = randomInt(),
  lineSequence: Int? = randomInt(),
  consecutiveToSequence: Int? = randomInt(),
  caseSequence: Int? = randomInt(),
  courtDescription: String? = randomString(),
  sentenceStatus: String? = randomString(),
  sentenceCategory: String? = randomString(),
  sentenceCalculationType: String? = randomString(),
  sentenceTypeDescription: String? = randomString(),
  sentenceDate: LocalDate? = randomLocalDate(),
  sentenceStartDate: LocalDate? = randomLocalDate(),
  sentenceEndDate: LocalDate? = randomLocalDate(),
  sentenceSequenceExpiryDate: LocalDate? = randomLocalDate(),
  terms: List<Term> = listOf(term()),
  offences: List<SentenceOffence> = listOf(sentenceOffence()),
  releaseDate: LocalDateTime? = randomLocalDateTime(),
  releasingPrison: String? = randomString(),
  licenceExpiryDate: LocalDate? = randomLocalDate(),
) = Sentence(
  bookingId = bookingId,
  sentenceSequence = sentenceSequence,
  lineSequence = lineSequence,
  consecutiveToSequence = consecutiveToSequence,
  caseSequence = caseSequence,
  courtDescription = courtDescription,
  sentenceStatus = sentenceStatus,
  sentenceCategory = sentenceCategory,
  sentenceCalculationType = sentenceCalculationType,
  sentenceTypeDescription = sentenceTypeDescription,
  sentenceDate = sentenceDate,
  sentenceStartDate = sentenceStartDate,
  sentenceEndDate = sentenceEndDate,
  sentenceSequenceExpiryDate = sentenceSequenceExpiryDate,
  terms = terms,
  offences = offences,
  releaseDate = releaseDate,
  releasingPrison = releasingPrison,
  licenceExpiryDate = licenceExpiryDate,
)

fun term(
  years: Int? = randomInt(),
  months: Int? = randomInt(),
  weeks: Int? = randomInt(),
  days: Int? = randomInt(),
  code: String? = randomString(),
) = Term(
  years = years,
  months = months,
  weeks = weeks,
  days = days,
  code = code,
)

fun sentenceOffence(
  offenderChargeId: Int? = randomInt(),
  offenceStartDate: LocalDate? = randomLocalDate(),
  offenceStatute: String? = randomString(),
  offenceCode: String? = randomString(),
  offenceDescription: String? = randomString(),
  indicators: List<String>? = listOf(randomString()),
) = SentenceOffence(
  offenderChargeId = offenderChargeId,
  offenceStartDate = offenceStartDate,
  offenceStatute = offenceStatute,
  offenceCode = offenceCode,
  offenceDescription = offenceDescription,
  indicators = indicators,
)
