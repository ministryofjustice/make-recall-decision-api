package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun ppudUserMappingEntity(
  id: Long = randomLong(),
  userName: String = randomString(),
  ppudUserFullName: String = randomString(),
  ppudTeamName: String = randomString(),
  ppudUserName: String = randomString(),
) = PpudUserMappingEntity(
  id = id,
  userName = userName,
  ppudTeamName = ppudTeamName,
  ppudUserFullName = ppudUserFullName,
  ppudUserName = ppudUserName,
)
