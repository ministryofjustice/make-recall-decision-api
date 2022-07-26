package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OffenderDetails(
  val firstName: String?,
  val surname: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val otherIds: OtherIds?
)

data class OtherIds(
  var crn: String?
)
