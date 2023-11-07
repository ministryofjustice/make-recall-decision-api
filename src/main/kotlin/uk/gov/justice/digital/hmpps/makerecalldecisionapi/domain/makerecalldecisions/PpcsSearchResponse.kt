package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PpcsSearchResponse(
  val results: List<PpcsSearchResult> = emptyList(),
)

data class PpcsSearchResult(
  val name: String,
  val crn: String,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val recommendationId: Long,
)
