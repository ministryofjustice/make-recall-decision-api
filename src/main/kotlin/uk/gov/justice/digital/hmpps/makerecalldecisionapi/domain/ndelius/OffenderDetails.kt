package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OffenderDetails(
  val firstName: String?,
  val surname: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val otherIds: OtherIds,
) {
  val isNameNullOrBlank = firstName.isNullOrBlank() && surname.isNullOrBlank()
}

data class OtherIds(
  var crn: String,
  val croNumber: String?,
  val mostRecentPrisonerNumber: String?,
  val pncNumber: String?,
  val nomsNumber: String?,
)
