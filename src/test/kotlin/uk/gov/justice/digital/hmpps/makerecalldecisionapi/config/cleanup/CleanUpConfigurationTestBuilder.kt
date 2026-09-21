package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.ZonedDateTime

internal fun cleanUpConfiguration(
  recurrent: RecurrentCleanUpConfiguration = recurrentCleanUpConfiguration(),
  newStandardLicenceConditions: NewStandardLicenceConditionsCleanUpConfiguration = newStandardLicenceConditions(),
) = CleanUpConfiguration(recurrent, newStandardLicenceConditions)

internal fun recurrentCleanUpConfiguration(
  lookBackInDays: Long = randomLong(),
) = RecurrentCleanUpConfiguration(lookBackInDays)

internal fun newStandardLicenceConditions(
  thresholdDateTime: ZonedDateTime = randomZonedDateTime(),
  cron: String = randomString(),
) = NewStandardLicenceConditionsCleanUpConfiguration(thresholdDateTime, cron)
