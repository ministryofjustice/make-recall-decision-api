package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum

fun indeterminateSentenceType(
  selected: IndeterminateSentenceTypeOptions? = randomEnum<IndeterminateSentenceTypeOptions>(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = IndeterminateSentenceType(
  selected = selected,
  allOptions = allOptions,
)
