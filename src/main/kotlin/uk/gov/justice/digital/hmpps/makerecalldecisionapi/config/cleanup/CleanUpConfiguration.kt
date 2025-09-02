package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clean-up")
data class CleanUpConfiguration(
  val recurrent: RecurrentCleanUpConfiguration,
)

data class RecurrentCleanUpConfiguration(
  val lookBackInDays: Long,
)
