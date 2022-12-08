package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PersonDetails(
  val fullName: String?,
  val name: String?,
  val firstName: String?,
  val middleNames: String?,
  val surname: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val age: Int?,
  val gender: String?,
  val crn: String?,
  val ethnicity: String?,
  val croNumber: String?,
  val mostRecentPrisonerNumber: String?,
  val pncNumber: String?,
  val nomsNumber: String?,
  val primaryLanguage: String?,
)
