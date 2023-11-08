package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudSearchResponse(
  val results: List<PpudSearchResult> = emptyList(),
)

data class PpudSearchResult(
  val id: String,
  val croNumber: String,
  val nomsId: String,
  val firstNames: String,
  val familyName: String,
  val dateOfBirth: LocalDate,
)
