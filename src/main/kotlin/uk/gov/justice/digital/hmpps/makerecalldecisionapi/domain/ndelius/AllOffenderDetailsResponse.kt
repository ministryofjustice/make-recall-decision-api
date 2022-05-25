package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AllOffenderDetailsResponse(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val surname: String? = null,
  val gender: String? = null,
  val contactDetails: ContactDetails?,
  val offenderManagers: List<OffenderManager>?
)
