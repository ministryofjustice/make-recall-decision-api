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

These are notes on the migration, to be added to the commit message. Reject if I
forgot to remove them from here (the application should fail to compile anyway).

SonarQube is already due to lose support at some point, as was announced in Slack
a while back, so we've decided to use this migration to remove it altogether. With
this, we also remove jacoco, which was generating the data for SonarQube. We haven't
been doing separate checks on the code coverage data jacoco produces, so there's no
point in keeping it. If we do need it in the future we can always re-add it.

The -Dkotlin.compiler.execution.strategy=in-process and -Dorg.gradle.workers.max=1
java options that existed in the CircleCI validate job were originally added for sonar,
hence why we aren't preserving them.

The resource_class option in the validate job was to handle the switch from H2 to
running postgres from a Docker container. Might be worth checking if this is still
needed when we move to testcontainers with MRD-2840.