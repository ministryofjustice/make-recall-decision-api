package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class LicenceCondition(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val startDate: LocalDate?,
  val terminationDate: LocalDate?,
  val createdDateTime: LocalDateTime?,
  val active: Boolean?,
  val licenceConditionNotes: String?,
  val licenceConditionTypeMainCat: LicenceConditionTypeMainCat?,
  val licenceConditionTypeSubCat: LicenceConditionTypeSubCat?,
)

data class LicenceConditionTypeMainCat(
  val code: String,
  val description: String,
)

data class LicenceConditionTypeSubCat(
  val code: String,
  val description: String,
)
