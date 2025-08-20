package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime

@ConfigurationProperties(prefix = "clean-up")
data class CleanUpConfiguration(
  val recurrent: RecurrentCleanUpConfiguration,
  val ftr48: FTR48CleanUpConfiguration,
)

data class RecurrentCleanUpConfiguration(
  val lookBackInDays: Long,
)

data class FTR48CleanUpConfiguration(
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mmZ")
  val thresholdDateTime: ZonedDateTime,
  val cron: String,
)
