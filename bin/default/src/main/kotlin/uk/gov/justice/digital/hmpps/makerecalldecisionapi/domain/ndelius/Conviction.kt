package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class Conviction(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val convictionDate: LocalDate?,
  val sentence: Sentence?,
  val active: Boolean?,
  val offences: List<Offence>?,
  val convictionId: Long?,
  val orderManagers: List<OrderManager>?,
  val custody: Custody? = null
) {
  val isCustodial: Boolean
    get() = custody != null
}
