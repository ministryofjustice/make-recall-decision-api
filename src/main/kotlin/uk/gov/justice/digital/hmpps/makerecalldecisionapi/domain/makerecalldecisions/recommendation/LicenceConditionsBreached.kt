package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.io.Serializable

data class LicenceConditionsBreached(
  val standardLicenceConditions: StandardLicenceConditions?,
  val additionalLicenceConditions: AdditionalLicenceConditions?,
) : Serializable

data class AdditionalLicenceConditions(
  @Deprecated(
    message = "selected field is deprecated, left this field in to allow existing recommendations to be backwards compatible",
    replaceWith = ReplaceWith("selectedOptions"),
  )
  val selected: List<String>? = null,
  val selectedOptions: List<SelectedOption>? = null,
  val allOptions: List<AdditionalLicenceConditionOption>? = null,
) : Serializable

data class SelectedOption(
  val mainCatCode: String,
  val subCatCode: String,
) : Serializable
