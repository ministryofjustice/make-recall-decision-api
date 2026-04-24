package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi

import java.time.LocalDate

// the prison API endpoint returns many more fields, but we're currently only interested in these
data class SentenceCalculationDates(
  val sentenceExpiryOverrideDate: LocalDate?,
  val sentenceExpiryCalculatedDate: LocalDate?,
)
