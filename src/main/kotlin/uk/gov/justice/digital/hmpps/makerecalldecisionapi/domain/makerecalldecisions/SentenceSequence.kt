package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class SentenceSequence(
  val indexSentence: Sentence,
  val sentencesInSequence: Map<Int, List<Sentence>>? = null,
)
