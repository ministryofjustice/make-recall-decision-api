package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence

data class SentenceSequence(
  val indexSentence: Sentence,
  val sentencesInSequence: MutableMap<Int, List<Sentence>>? = null,
)
