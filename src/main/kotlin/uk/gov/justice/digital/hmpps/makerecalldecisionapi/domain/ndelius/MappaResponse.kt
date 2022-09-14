package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.LocalDate

data class MappaResponse(
  val level: Int?,
  val levelDescription: String?,
  val category: Int?,
  val categoryDescription: String?,
  val startDate: LocalDate?,
  val reviewDate: LocalDate?,
  val team: Team?,
  val officer: Officer?,
  val probationArea: ProbationArea?,
  val notes: String?
)

data class Officer(
  val code: String?,
  val forenames: String?,
  val surname: String?,
  val unallocated: Boolean?
)
