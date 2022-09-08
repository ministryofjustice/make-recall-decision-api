package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate

data class Offence @JsonCreator constructor(
  val mainOffence: Boolean?,
  val detail: OffenceDetail?,
  val offenceDate: LocalDate?,
)
