package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime

@ConfigurationProperties(prefix = "clean-up")
data class CleanUpConfiguration(
  val recurrent: RecurrentCleanUpConfiguration,
  val ftr56: FTR56CleanUpConfiguration,
)

data class RecurrentCleanUpConfiguration(
  val lookBackInDays: Long,
)

/**
 * The threshold date-time to use when calculating what ongoing recommendations to delete.
 *
 * The regular clean-up task already deletes ongoing recommendations regularly, but only those older than the
 * lookBackInDays value configured. In order to avoid clashes between the regular clean-up task and the FTR56 one, we
 * set up the FTR56 one such that it only cleans up ongoing recommendations that are older than the thresholdDateTime
 * configured here (i.e. the time of the switchover) but not older than thresholdDateTime minus (lookBackInDays - 1).
 *
 * @param thresholdDateTime the date and time of the switchover, in ISO format (e.g. 2026-03-30T23:00Z)
 * @param cron the cron expression to use for the FTR56 clean-up task, which should be set to the switchover time (this
 *             is set in Europe/London time).
 */
data class FTR56CleanUpConfiguration(
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mmZ")
  val thresholdDateTime: ZonedDateTime,
  val cron: String,
)
