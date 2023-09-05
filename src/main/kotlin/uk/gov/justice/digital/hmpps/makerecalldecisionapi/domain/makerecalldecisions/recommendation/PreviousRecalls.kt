package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PreviousRecalls(
  @JsonFormat(pattern = "yyyy-MM-dd") val lastRecallDate: LocalDate? = null,
  val hasBeenRecalledPreviously: Boolean? = null,
  @JsonFormat(pattern = "yyyy-MM-dd") val previousRecallDates: List<LocalDate>? = null,
)
