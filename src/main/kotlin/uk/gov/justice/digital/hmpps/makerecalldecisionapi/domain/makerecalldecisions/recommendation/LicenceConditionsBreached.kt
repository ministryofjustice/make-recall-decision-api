package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class LicenceConditionsBreached(
  val standardLicenceConditions: StandardLicenceConditions?,
  val additionalLicenceConditions: List<AdditionalLicenceCondition>? = null
)
