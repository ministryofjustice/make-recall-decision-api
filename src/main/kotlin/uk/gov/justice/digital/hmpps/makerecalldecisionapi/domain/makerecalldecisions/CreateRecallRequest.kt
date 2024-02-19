package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDateTime

data class CreateRecallRequest(
  val decisionDateTime: LocalDateTime,
  val isExtendedSentence: Boolean,
  val isInCustody: Boolean,
  val mappaLevel: String,
  val policeForce: String,
  val probationArea: String,
  val receivedDateTime: LocalDateTime,
  val riskOfContrabandDetails: String = "",
  val riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel,
)
