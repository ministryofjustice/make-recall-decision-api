package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.LocalDateTime

data class OrderManager(
  val dateStartOfAllocation: LocalDateTime?,
  val name: String?,
  val staffCode: String?,
  val gradeCode: String?,
)
