package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

/**
 * Helper functions for generating instances of PpudCreateSentenceResponse
 * with their fields pre-filled with random values. Intended for use in unit tests.
 */

fun ppudCreateSentenceResponse(
  sentence: PpudCreatedSentence = ppudCreatedSentence(),
) = PpudCreateSentenceResponse(sentence)
