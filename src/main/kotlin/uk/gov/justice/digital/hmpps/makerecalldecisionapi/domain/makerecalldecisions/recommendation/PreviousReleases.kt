package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDate

data class PreviousReleases(
  @param:JsonFormat(pattern = "yyyy-MM-dd") val lastReleaseDate: LocalDate? = null,
  val lastReleasingPrisonOrCustodialEstablishment: String? = null,
  val hasBeenReleasedPreviously: Boolean? = null,
  @param:JsonFormat(pattern = "yyyy-MM-dd") val previousReleaseDates: List<LocalDate>? = null,
) : Serializable
