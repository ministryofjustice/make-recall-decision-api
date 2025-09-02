package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

// shedlock to be enabled when roll-outs required a single task run - e.g. see MRD-2804
// @EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@EnableScheduling
@EnableRetry
@ConfigurationPropertiesScan
@SpringBootApplication
class MakeRecallDecisionApi

fun main(args: Array<String>) {
  runApplication<MakeRecallDecisionApi>(*args)
}
