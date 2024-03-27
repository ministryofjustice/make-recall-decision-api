package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// import org.springframework.scheduling.annotation.EnableScheduling

// @EnableScheduling
@SpringBootApplication()
class MakeRecallDecisionApi

fun main(args: Array<String>) {
  runApplication<MakeRecallDecisionApi>(*args)
}
