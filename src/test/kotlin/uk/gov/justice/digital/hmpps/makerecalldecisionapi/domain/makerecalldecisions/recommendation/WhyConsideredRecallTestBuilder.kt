package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum

fun whyConsideredRecall(
  selected: WhyConsideredRecallValue? = randomEnum<WhyConsideredRecallValue>(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = WhyConsideredRecall(
  selected = selected,
  allOptions = allOptions,
)
