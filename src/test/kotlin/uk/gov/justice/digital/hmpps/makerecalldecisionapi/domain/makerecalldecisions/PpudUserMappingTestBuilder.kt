package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun ppudUserMapping(
  id: String = randomLong().toString(),
  userName: String = randomString(),
  ppudUserFullName: String = randomString(),
  ppudTeamName: String = randomString(),
  ppudUserName: String = randomString(),
) = PpudUserMapping(
  id = id,
  userName = userName,
  ppudUserFullName = ppudUserFullName,
  ppudTeamName = ppudTeamName,
  ppudUserName = ppudUserName,
)