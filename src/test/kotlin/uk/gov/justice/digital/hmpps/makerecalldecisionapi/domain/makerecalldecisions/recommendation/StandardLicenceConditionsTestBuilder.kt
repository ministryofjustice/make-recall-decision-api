package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun standardLicenceConditions(
  selected: List<String>? = listOf(randomString()),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = StandardLicenceConditions(
  selected = selected,
  allOptions = allOptions,
)
