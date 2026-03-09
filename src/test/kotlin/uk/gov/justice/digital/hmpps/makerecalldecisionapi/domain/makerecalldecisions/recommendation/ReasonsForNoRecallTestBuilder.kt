package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun reasonsForNoRecall(
  licenceBreach: String? = randomString(),
  noRecallRationale: String? = randomString(),
  popProgressMade: String? = randomString(),
  popThoughts: String? = randomString(),
  futureExpectations: String? = randomString(),
) = ReasonsForNoRecall(
  licenceBreach = licenceBreach,
  noRecallRationale = noRecallRationale,
  popProgressMade = popProgressMade,
  popThoughts = popThoughts,
  futureExpectations = futureExpectations,
)
