package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt

/**
 * Helper functions for generating instances of PpudYearMonth with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun ppudYearMonth(
  years: Int = randomInt(),
  months: Int = randomInt(),
) = PpudYearMonth(years, months)

internal fun PpudYearMonth.toJson() = json(
  """
      {
        "years": $years,
        "months": $months
      }
  """.trimIndent(),
)
