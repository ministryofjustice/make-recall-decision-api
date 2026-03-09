package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun recallConsidered(
  id: Long = randomLong(),
  userId: String? = randomString(),
  createdDate: String? = randomLocalDate().toString(),
  userName: String? = randomString(),
  recallConsideredDetail: String? = randomString(),
) = RecallConsidered(
  id = id,
  userId = userId,
  createdDate = createdDate,
  userName = userName,
  recallConsideredDetail = recallConsideredDetail,
)
