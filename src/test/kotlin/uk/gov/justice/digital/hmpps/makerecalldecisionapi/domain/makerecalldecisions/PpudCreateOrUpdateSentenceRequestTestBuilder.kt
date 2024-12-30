package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

/**
 * Helper functions for generating instances of classes related to
 * PPUD Automation responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

fun ppudCreateOrUpdateSentenceRequest(
  custodyType: String = randomString(),
  dateOfSentence: LocalDate = randomLocalDate(),
  licenceExpiryDate: LocalDate? = randomLocalDate(),
  mappaLevel: String = randomString(),
  releaseDate: LocalDate? = randomLocalDate(),
  sentenceLength: SentenceLength? = sentenceLength(),
  espCustodialPeriod: PpudYearMonth? = ppudYearMonth(),
  espExtendedPeriod: PpudYearMonth? = ppudYearMonth(),
  sentenceExpiryDate: LocalDate? = randomLocalDate(),
  sentencingCourt: String = randomString(),
): PpudCreateOrUpdateSentenceRequest {
  return PpudCreateOrUpdateSentenceRequest(
    custodyType,
    dateOfSentence,
    licenceExpiryDate,
    mappaLevel,
    releaseDate,
    sentenceLength,
    espCustodialPeriod,
    espExtendedPeriod,
    sentenceExpiryDate,
    sentencingCourt,
  )
}