package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class IndeterminateOrExtendedSentenceDetails(
  val selected: List<ValueWithDetails>?,
  val allOptions: List<TextValueOption>? = null,
)

enum class IndeterminateOrExtendedSentenceDetailsOptions {
  BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE,
  BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE,
  OUT_OF_TOUCH,
}
