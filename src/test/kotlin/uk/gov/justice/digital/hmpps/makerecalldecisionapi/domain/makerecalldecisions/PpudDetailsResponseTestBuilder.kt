package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt

/**
 * Helper functions for generating instances of classes related to
 * PPUD Details responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

fun sentenceLength(
  partYears: Int? = randomInt(),
  partMonths: Int? = randomInt(),
  partDays: Int? = randomInt(),
): SentenceLength {
  return SentenceLength(partYears, partMonths, partDays)
}