package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.LocalDate

data class Custody(
  val bookingNumber: String?,
  val institution: Institution?,
  val status: CustodyStatus?,
  val keyDates: KeyDates?,
  val sentenceStartDate: LocalDate?
)
