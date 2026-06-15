package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import java.time.LocalDate
import java.time.LocalDateTime

fun nomisIndexOffence(
  selected: Int? = randomInt(),
  allOptions: List<OfferedOffence>? = listOf(offeredOffence()),
) = NomisIndexOffence(
  selected = selected,
  allOptions = allOptions,
)

fun offeredOffence(
  consecutiveCount: Int? = null,
  offenderChargeId: Int? = null,
  offenceCode: String? = null,
  offenceStatute: String? = null,
  offenceDescription: String? = null,
  offenceDate: LocalDate? = null,
  sentenceDate: LocalDate? = null,
  courtDescription: String? = null,
  sentenceStartDate: LocalDate? = null,
  sentenceEndDate: LocalDate? = null,
  bookingId: Int? = null,
  terms: List<Term>? = null,
  sentenceTypeDescription: String? = null,
  releaseDate: LocalDateTime? = null,
  releasingPrison: String? = null,
  licenceExpiryDate: LocalDate? = null,
) = OfferedOffence(
  consecutiveCount = consecutiveCount,
  offenderChargeId = offenderChargeId,
  offenceCode = offenceCode,
  offenceStatute = offenceStatute,
  offenceDescription = offenceDescription,
  offenceDate = offenceDate,
  sentenceDate = sentenceDate,
  courtDescription = courtDescription,
  sentenceStartDate = sentenceStartDate,
  sentenceEndDate = sentenceEndDate,
  bookingId = bookingId,
  terms = terms,
  sentenceTypeDescription = sentenceTypeDescription,
  releaseDate = releaseDate,
  releasingPrison = releasingPrison,
  licenceExpiryDate = licenceExpiryDate,
)
