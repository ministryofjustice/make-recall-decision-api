package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDateTime

data class PpudCreateRecallRequest(
  val decisionDateTime: LocalDateTime,
  val isExtendedSentence: Boolean,
  val isInCustody: Boolean,
  val mappaLevel: String,
  val policeForce: String,
  val probationArea: String,
  val receivedDateTime: LocalDateTime,
  val recommendedTo: PpudUser = PpudUser("", ""),
  val riskOfContrabandDetails: String = "",
  val riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel,
)

enum class RiskOfSeriousHarmLevel(val descriptor: String) {
  Low("Low"),
  Medium("Medium"),
  High("High"),
  VeryHigh("Very High"),
  NotApplicable("Not Applicable"),
}
