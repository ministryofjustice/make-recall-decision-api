package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomFutureLocalDate
import java.time.LocalDate

internal fun sentenceCalculationDates(
  sentenceExpiryOverrideDate: LocalDate? = randomFutureLocalDate(),
  sentenceExpiryCalculatedDate: LocalDate? = randomFutureLocalDate(),
) = SentenceCalculationDates(
  sentenceExpiryOverrideDate,
  sentenceExpiryCalculatedDate,
)
