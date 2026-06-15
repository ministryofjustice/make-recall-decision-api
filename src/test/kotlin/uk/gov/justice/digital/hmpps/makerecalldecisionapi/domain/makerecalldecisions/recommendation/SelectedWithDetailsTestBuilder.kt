package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun selectedWithDetails(
  selected: Boolean? = randomBoolean(),
  details: String? = randomString(),
) = SelectedWithDetails(
  selected = selected,
  details = details,
)
