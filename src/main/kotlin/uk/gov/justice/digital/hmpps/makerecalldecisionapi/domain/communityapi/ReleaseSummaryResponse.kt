package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class ReleaseSummaryResponse(
  val lastRelease: LastRelease?,
  val lastRecall: LastRecall?
)

data class LastRelease(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val date: LocalDate?,
)

data class LastRecall(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val date: LocalDate?,
)
