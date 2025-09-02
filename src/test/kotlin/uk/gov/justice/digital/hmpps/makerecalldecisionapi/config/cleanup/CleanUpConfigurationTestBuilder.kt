package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong

internal fun cleanUpConfiguration(
  recurrent: RecurrentCleanUpConfiguration = recurrentCleanUpConfiguration(),
) = CleanUpConfiguration(recurrent)

internal fun recurrentCleanUpConfiguration(
  lookBackInDays: Long = randomLong(),
) = RecurrentCleanUpConfiguration(lookBackInDays)
