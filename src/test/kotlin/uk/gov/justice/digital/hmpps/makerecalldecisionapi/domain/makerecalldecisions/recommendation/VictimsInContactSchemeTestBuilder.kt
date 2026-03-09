package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum

fun victimsInContactScheme(
  selected: YesNoNotApplicableOptions? = randomEnum<YesNoNotApplicableOptions>(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = VictimsInContactScheme(
  selected = selected,
  allOptions = allOptions,
)
