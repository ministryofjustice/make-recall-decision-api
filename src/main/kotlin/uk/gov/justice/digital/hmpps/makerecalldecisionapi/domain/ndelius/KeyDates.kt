package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.LocalDate

data class KeyDates(
  val conditionalReleaseDate: LocalDate?,
  val expectedPrisonOffenderManagerHandoverDate: LocalDate?,
  val expectedPrisonOffenderManagerHandoverStartDate: LocalDate?,
  val expectedReleaseDate: LocalDate?,
  val hdcEligibilityDate: LocalDate?,
  val licenceExpiryDate: LocalDate?,
  val paroleEligibilityDate: LocalDate?,
  val sentenceExpiryDate: LocalDate?,
  val postSentenceSupervisionEndDate: LocalDate?
)
