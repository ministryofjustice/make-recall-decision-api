package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun additionalLicenceConditionOption(
  subCatCode: String? = randomString(),
  mainCatCode: String? = randomString(),
  title: String? = randomString(),
  details: String? = randomString(),
  note: String? = randomString(),
) = AdditionalLicenceConditionOption(
  subCatCode = subCatCode,
  mainCatCode = mainCatCode,
  title = title,
  details = details,
  note = note,
)
