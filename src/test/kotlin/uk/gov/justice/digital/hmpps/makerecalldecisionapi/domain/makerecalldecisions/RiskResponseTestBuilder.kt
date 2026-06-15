package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

fun mappa(
  level: Int? = randomInt(),
  lastUpdatedDate: LocalDate? = randomLocalDate(),
  category: Int? = randomInt(),
  error: String? = randomString(),
  hasBeenReviewed: Boolean? = randomBoolean(),
) = Mappa(
  level = level,
  lastUpdatedDate = lastUpdatedDate,
  category = category,
  error = error,
  hasBeenReviewed = hasBeenReviewed,
)

fun riskOfSeriousHarm(
  overallRisk: String? = randomString(),
  riskInCustody: RiskTo? = riskTo(),
  riskInCommunity: RiskTo? = riskTo(),
) = RiskOfSeriousHarm(
  overallRisk = overallRisk,
  riskInCustody = riskInCustody,
  riskInCommunity = riskInCommunity,
)

fun riskTo(
  riskToChildren: String? = randomString(),
  riskToPublic: String? = randomString(),
  riskToKnownAdult: String? = randomString(),
  riskToStaff: String? = randomString(),
  riskToPrisoners: String? = randomString(),
) = RiskTo(
  riskToChildren = riskToChildren,
  riskToPublic = riskToPublic,
  riskToKnownAdult = riskToKnownAdult,
  riskToStaff = riskToStaff,
  riskToPrisoners = riskToPrisoners,
)

fun roshSummary(
  natureOfRisk: String? = randomString(),
  whoIsAtRisk: String? = randomString(),
  riskIncreaseFactors: String? = randomString(),
  riskMitigationFactors: String? = randomString(),
  riskImminence: String? = randomString(),
  riskOfSeriousHarm: RiskOfSeriousHarm? = riskOfSeriousHarm(),
  lastUpdatedDate: String? = randomString(),
  error: String? = randomString(),
) = RoshSummary(
  natureOfRisk = natureOfRisk,
  whoIsAtRisk = whoIsAtRisk,
  riskIncreaseFactors = riskIncreaseFactors,
  riskMitigationFactors = riskMitigationFactors,
  riskImminence = riskImminence,
  riskOfSeriousHarm = riskOfSeriousHarm,
  lastUpdatedDate = lastUpdatedDate,
  error = error,
)
