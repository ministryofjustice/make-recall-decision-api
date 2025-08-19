package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.ZonedDateTime

internal fun cleanUpConfiguration(
  ftr48: FTR48CleanUpConfiguration = ftr48CleanUpConfigurationTestBuilder(),
) = CleanUpConfiguration(ftr48)

internal fun ftr48CleanUpConfigurationTestBuilder(
  thresholdDateTime: ZonedDateTime = randomZonedDateTime(),
  cron: String = randomString(),
) = FTR48CleanUpConfiguration(thresholdDateTime, cron)