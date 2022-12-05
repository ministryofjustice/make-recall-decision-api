package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate

data class PreviousRecalls(
  val lastRecallDate: LocalDate? = null,
  val hasBeenRecalledPreviously: Boolean? = null,
  val previousRecallDates: List<LocalDate>? = null
)
