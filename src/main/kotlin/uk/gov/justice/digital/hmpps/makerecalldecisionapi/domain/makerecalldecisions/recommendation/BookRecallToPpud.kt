package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate
import java.time.LocalDateTime

data class BookRecallToPpud(
  val decisionDateTime: LocalDateTime,
  val isInCustody: Boolean,
  val mappaLevel: String,
  val policeForce: String,
  val probationArea: String,
  val recommendedToOwner: String,
  val receivedDateTime: String,
  val releaseDate: LocalDate,
  val riskOfContrabandDetails: String,
  val riskOfSeriousHarmLevel: String,
  val sentenceDate: LocalDate,
)
