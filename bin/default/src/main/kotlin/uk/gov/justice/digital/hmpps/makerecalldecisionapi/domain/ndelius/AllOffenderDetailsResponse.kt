package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AllOffenderDetailsResponse(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val surname: String? = null,
  val middleNames: List<String>?,
  val gender: String? = null,
  val contactDetails: ContactDetails?,
  val offenderManagers: List<OffenderManager>?,
  val offenderProfile: OffenderProfile?,
  val otherIds: OtherIds?
)

data class OffenderProfile(
  val ethnicity: String? = null,
  val offenderLanguages: OffenderLanguages? = null,
)

data class OffenderLanguages(
  val primaryLanguage: String? = null
)
