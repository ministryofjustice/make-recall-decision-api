package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PreviousReleases(
  val lastReleaseDate: LocalDate? = null,
  val lastReleasingPrisonOrCustodialEstablishment: String? = null,
  val previousReleaseDates: List<String>? = null
)
