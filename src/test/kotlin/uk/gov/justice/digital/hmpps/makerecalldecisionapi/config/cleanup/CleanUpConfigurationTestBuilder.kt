package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.ZonedDateTime

internal fun cleanUpConfiguration(
  recurrent: RecurrentCleanUpConfiguration = recurrentCleanUpConfiguration(),
  ftr48: FTR48CleanUpConfiguration = ftr48CleanUpConfiguration(),
) = CleanUpConfiguration(recurrent, ftr48)

internal fun recurrentCleanUpConfiguration(
  lookBackInDays: Long = randomLong(),
) = RecurrentCleanUpConfiguration(lookBackInDays)

internal fun ftr48CleanUpConfiguration(
  thresholdDateTime: ZonedDateTime = randomZonedDateTime(),
  cron: String = randomString(),
) = FTR48CleanUpConfiguration(thresholdDateTime, cron)
