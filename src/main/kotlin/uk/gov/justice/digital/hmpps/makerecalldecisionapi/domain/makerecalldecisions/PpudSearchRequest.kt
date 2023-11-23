package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudSearchRequest(
  val croNumber: String?,
  val nomsId: String?,
  val familyName: String,
  val dateOfBirth: LocalDate,
)
