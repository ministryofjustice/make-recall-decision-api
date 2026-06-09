package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDate

data class PreviousRecalls(
  @param:JsonFormat(pattern = "yyyy-MM-dd") val lastRecallDate: LocalDate? = null,
  val hasBeenRecalledPreviously: Boolean? = null,
  @param:JsonFormat(pattern = "yyyy-MM-dd") val previousRecallDates: List<LocalDate>? = null,
) : Serializable
