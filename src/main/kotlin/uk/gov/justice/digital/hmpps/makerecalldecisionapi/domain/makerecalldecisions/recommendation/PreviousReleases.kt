package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate

data class PreviousReleases(
  val lastReleaseDate: LocalDate? = null,
  val lastReleasingPrisonOrCustodialEstablishment: String? = null,
  val hasBeenReleasedPreviously: Boolean? = null,
  val previousReleaseDates: List<LocalDate>? = null
)
