package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudCreateOrUpdateRelease(
  val dateOfRelease: LocalDate,
  val postRelease: UpdatePostRelease = UpdatePostRelease(),
  val releasedFrom: String,
  val releasedUnder: String,
)
