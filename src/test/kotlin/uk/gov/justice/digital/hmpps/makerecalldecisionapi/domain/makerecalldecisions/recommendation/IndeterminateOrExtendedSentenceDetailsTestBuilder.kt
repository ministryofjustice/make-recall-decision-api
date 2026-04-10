package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption

fun indeterminateOrExtendedSentenceDetails(
  selected: List<ValueWithDetails>? = listOf(valueWithDetails()),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = IndeterminateOrExtendedSentenceDetails(
  selected = selected,
  allOptions = allOptions,
)
