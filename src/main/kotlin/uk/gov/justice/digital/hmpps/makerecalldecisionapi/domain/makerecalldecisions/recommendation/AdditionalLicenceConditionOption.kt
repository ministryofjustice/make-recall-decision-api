package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.io.Serializable

data class AdditionalLicenceConditionOption(
  val subCatCode: String? = null,
  val mainCatCode: String? = null,
  val title: String? = null,
  val details: String? = null,
  val note: String? = null,
) : Serializable
