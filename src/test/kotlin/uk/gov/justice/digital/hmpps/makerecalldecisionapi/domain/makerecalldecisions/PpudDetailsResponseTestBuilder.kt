package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt

/**
 * Helper functions for generating instances of classes related to
 * PPUD Details responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

internal fun sentenceLength(
  partYears: Int? = randomInt(),
  partMonths: Int? = randomInt(),
  partDays: Int? = randomInt(),
) =
  SentenceLength(partYears, partMonths, partDays)

internal fun SentenceLength.toJson() =
  json(
    """
      {
        "partYears": $partYears,
        "partMonths": $partMonths,
        "partDays": $partDays
      }
    """.trimIndent(),
  )
