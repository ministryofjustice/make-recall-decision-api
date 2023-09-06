package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PreviousReleases(
  @JsonFormat(pattern = "yyyy-MM-dd") val lastReleaseDate: LocalDate? = null,
  val lastReleasingPrisonOrCustodialEstablishment: String? = null,
  val hasBeenReleasedPreviously: Boolean? = null,
  @JsonFormat(pattern = "yyyy-MM-dd") val previousReleaseDates: List<LocalDate>? = null,
)
