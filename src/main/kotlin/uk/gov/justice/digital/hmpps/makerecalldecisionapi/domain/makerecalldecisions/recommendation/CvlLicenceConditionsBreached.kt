package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class CvlLicenceConditionsBreached(
  val standardLicenceConditions: LicenceConditionSection?,
  val additionalLicenceConditions: LicenceConditionSection?,
  val bespokeLicenceConditions: LicenceConditionSection?,
)

data class LicenceConditionSection(
  val selected: List<String>? = null,
  val allOptions: List<LicenceConditionOption>? = null,
)

data class LicenceConditionOption(
  val code: String,
  val text: String,
)
