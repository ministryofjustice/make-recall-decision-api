package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt

/**
 * Helper functions for generating instances of PpudYearMonth with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

fun ppudYearMonth(
  years: Int = randomInt(),
  months: Int = randomInt(),
): PpudYearMonth {
  return PpudYearMonth(years, months)
}