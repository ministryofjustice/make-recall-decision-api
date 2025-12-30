package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt

fun sentenceSequence(
  indexSentence: Sentence = sentence(),
  sentencesInSequence: MutableMap<Int, List<Sentence>>? = mutableMapOf(randomInt() to mutableListOf(sentence())),
) = SentenceSequence(
  indexSentence = indexSentence,
  sentencesInSequence = sentencesInSequence,
)
