package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun licenceConditionsBreached(
  standardLicenceConditions: StandardLicenceConditions? = standardLicenceConditions(),
  additionalLicenceConditions: AdditionalLicenceConditions? = additionalLicenceConditions(),
) = LicenceConditionsBreached(
  standardLicenceConditions = standardLicenceConditions,
  additionalLicenceConditions = additionalLicenceConditions,
)

fun additionalLicenceConditions(
  selected: List<String>? = listOf(randomString()),
  selectedOptions: List<SelectedOption>? = listOf(selectedOption()),
  allOptions: List<AdditionalLicenceConditionOption>? = listOf(additionalLicenceConditionOption()),
) = AdditionalLicenceConditions(
  selected = selected,
  selectedOptions = selectedOptions,
  allOptions = allOptions,
)

fun selectedOption(
  mainCatCode: String = randomString(),
  subCatCode: String = randomString(),
) = SelectedOption(
  mainCatCode = mainCatCode,
  subCatCode = subCatCode,
)
