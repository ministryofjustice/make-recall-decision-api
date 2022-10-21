package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl

import java.time.LocalDate

data class LicenceConditionResponse(
  val conditionalReleaseDate: LocalDate? = null,
  val actualReleaseDate: LocalDate? = null,
  val sentenceStartDate: LocalDate? = null,
  val sentenceEndDate: LocalDate? = null,
  val licenceStartDate: LocalDate? = null,
  val licenceExpiryDate: LocalDate? = null,
  val topupSupervisionStartDate: LocalDate? = null,
  val topupSupervisionExpiryDate: LocalDate? = null,
  val standardLicenceConditions: List<LicenceConditionDetail>? = null,
  val standardPssConditions: List<LicenceConditionDetail>? = null,
  val additionalLicenceConditions: List<LicenceConditionDetail>? = null,
  val additionalPssConditions: List<LicenceConditionDetail>? = null,
  val bespokeConditions: List<LicenceConditionDetail>? = null
)

data class LicenceConditionDetail(
  val text: String? = null,
  val expandedText: String? = null
)
