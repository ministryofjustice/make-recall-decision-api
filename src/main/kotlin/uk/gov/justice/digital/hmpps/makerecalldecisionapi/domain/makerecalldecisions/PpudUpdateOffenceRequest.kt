package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudUpdateOffenceRequest(
  val indexOffence: String,
  val dateOfIndexOffence: LocalDate?,
)
