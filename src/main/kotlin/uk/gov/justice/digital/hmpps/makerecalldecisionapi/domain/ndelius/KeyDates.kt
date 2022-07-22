package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.LocalDate

data class KeyDates(
  val licenceExpiryDate: LocalDate?,
  val sentenceExpiryDate: LocalDate?,
  val postSentenceSupervisionEndDate: LocalDate?
)
