package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

data class LicenceCondition(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val startDate: LocalDate,
  val terminationDate: LocalDate? = null,
  val createdDateTime: LocalDateTime? = null,
  val active: Boolean? = null,
  @JsonProperty("notes")
  val licenceConditionNotes: String?,
  @JsonProperty("mainCategory")
  val licenceConditionTypeMainCat: LicenceConditionTypeMainCat?,
  @JsonProperty("subCategory")
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
