package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of classes related to
 * PPUDCreateOffenderResponse with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

fun ppudCreatedSentence(
  id: String? = randomString(),
): PpudCreatedSentence {
  return PpudCreatedSentence(id)
}