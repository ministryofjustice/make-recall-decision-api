package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun cvlLicenceConditionsBreached(
  standardLicenceConditions: LicenceConditionSection? = licenceConditionSection(),
  additionalLicenceConditions: LicenceConditionSection? = licenceConditionSection(),
  bespokeLicenceConditions: LicenceConditionSection? = licenceConditionSection(),
) = CvlLicenceConditionsBreached(
  standardLicenceConditions = standardLicenceConditions,
  additionalLicenceConditions = additionalLicenceConditions,
  bespokeLicenceConditions = bespokeLicenceConditions,
)

fun licenceConditionSection(
  selected: List<String>? = listOf(randomString()),
  allOptions: List<LicenceConditionOption>? = listOf(licenceConditionOption()),
) = LicenceConditionSection(
  selected = selected,
  allOptions = allOptions,
)

fun licenceConditionOption(
  code: String? = randomString(),
  text: String? = randomString(),
) = LicenceConditionOption(
  code = code,
  text = text,
)
