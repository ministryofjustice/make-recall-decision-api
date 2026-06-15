package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun custodyStatus(
  selected: CustodyStatusValue? = randomEnum<CustodyStatusValue>(),
  details: String? = randomString(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = CustodyStatus(
  selected = selected,
  details = details,
  allOptions = allOptions,
)
