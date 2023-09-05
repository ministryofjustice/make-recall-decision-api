package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl

data class LicenceConditionCvlResponse(
  val conditionalReleaseDate: String? = null,
  val actualReleaseDate: String? = null,
  val sentenceStartDate: String? = null,
  val sentenceEndDate: String? = null,
  val licenceStartDate: String? = null,
  val licenceExpiryDate: String? = null,
  val topupSupervisionStartDate: String? = null,
  val topupSupervisionExpiryDate: String? = null,
  val standardLicenceConditions: List<LicenceConditionCvlDetail>? = null,
  val standardPssConditions: List<LicenceConditionCvlDetail>? = null,
  val additionalLicenceConditions: List<LicenceConditionCvlDetail>? = null,
  val additionalPssConditions: List<LicenceConditionCvlDetail>? = null,
  val bespokeConditions: List<LicenceConditionCvlDetail>? = null,
)

data class LicenceConditionCvlDetail(
  val code: String? = null,
  val text: String? = null,
  val expandedText: String? = null,
  val category: String? = null,
)
