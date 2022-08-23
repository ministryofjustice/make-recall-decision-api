package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class LicenceConditionsBreached(
  val standardLicenceConditions: StandardLicenceConditions?,
  val additionalLicenceConditions: AdditionalLicenceConditions?
)

data class AdditionalLicenceConditions(
  val selected: List<String>? = null,
  val allOptions: List<AdditionalLicenceConditionOption>? = null
)
