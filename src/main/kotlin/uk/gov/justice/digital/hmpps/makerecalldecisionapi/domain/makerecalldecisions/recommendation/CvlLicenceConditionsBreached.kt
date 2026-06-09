package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.io.Serializable

data class CvlLicenceConditionsBreached(
  val standardLicenceConditions: LicenceConditionSection?,
  val additionalLicenceConditions: LicenceConditionSection?,
  val bespokeLicenceConditions: LicenceConditionSection?,
) : Serializable

data class LicenceConditionSection(
  val selected: List<String>? = null,
  val allOptions: List<LicenceConditionOption>? = null,
) : Serializable

data class LicenceConditionOption(
  val code: String?,
  val text: String?,
) : Serializable
