package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun valueWithDetails(
  value: String? = randomString(),
  details: String? = randomString(),
) = ValueWithDetails(
  value = value,
  details = details,
)
