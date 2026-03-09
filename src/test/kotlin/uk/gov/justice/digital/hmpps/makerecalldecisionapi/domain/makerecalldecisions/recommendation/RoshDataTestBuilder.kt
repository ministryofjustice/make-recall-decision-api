package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum

fun roshData(
  riskToChildren: RoshDataScore? = randomEnum<RoshDataScore>(),
  riskToPublic: RoshDataScore? = randomEnum<RoshDataScore>(),
  riskToKnownAdult: RoshDataScore? = randomEnum<RoshDataScore>(),
  riskToStaff: RoshDataScore? = randomEnum<RoshDataScore>(),
  riskToPrisoners: RoshDataScore? = randomEnum<RoshDataScore>(),
) = RoshData(
  riskToChildren = riskToChildren,
  riskToPublic = riskToPublic,
  riskToKnownAdult = riskToKnownAdult,
  riskToStaff = riskToStaff,
  riskToPrisoners = riskToPrisoners,
)
