package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

fun previousReleases(
  lastReleaseDate: LocalDate? = randomLocalDate(),
  lastReleasingPrisonOrCustodialEstablishment: String? = randomString(),
  hasBeenReleasedPreviously: Boolean? = randomBoolean(),
  previousReleaseDates: List<LocalDate>? = listOf(randomLocalDate()),
) = PreviousReleases(
  lastReleaseDate = lastReleaseDate,
  lastReleasingPrisonOrCustodialEstablishment = lastReleasingPrisonOrCustodialEstablishment,
  hasBeenReleasedPreviously = hasBeenReleasedPreviously,
  previousReleaseDates = previousReleaseDates,
)
