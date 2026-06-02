package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

data class LicenceCondition(
  @param:JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val startDate: LocalDate,
  val terminationDate: LocalDate? = null,
  val createdDateTime: LocalDateTime? = null,
  val active: Boolean? = null,
  @param:JsonProperty("notes")
  val licenceConditionNotes: String?,
  @param:JsonProperty("mainCategory")
  val licenceConditionTypeMainCat: LicenceConditionTypeMainCat?,
  @param:JsonProperty("subCategory")
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
