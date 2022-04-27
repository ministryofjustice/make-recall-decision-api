package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import uk.gov.justice.digital.hmpps.hmppsallocations.domain.Offence
import uk.gov.justice.digital.hmpps.hmppsallocations.domain.Sentence
import java.time.LocalDate
import java.time.LocalDateTime

data class Conviction(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val convictionDate: LocalDate?,
  val sentence: Sentence?,
  val active: Boolean,
  val offences: List<Offence>,
  val convictionId: Long,
  val orderManagers: List<OrderManager>,
  val custody: Custody?
)

data class OrderManager(
  val dateStartOfAllocation: LocalDateTime?,
  val name: String?,
  val staffCode: String?,
  val gradeCode: String?
)

data class Custody(
  val status: CustodyStatus
)

data class CustodyStatus(
  val code: String
)
