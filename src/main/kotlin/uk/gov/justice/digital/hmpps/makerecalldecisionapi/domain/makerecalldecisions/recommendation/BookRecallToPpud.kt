package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.time.LocalDate
import java.time.LocalDateTime

data class BookRecallToPpud(
  val decisionDateTime: LocalDateTime? = null,
  val isInCustody: Boolean? = null,
  val custodyType: String? = null,
  val indexOffence: String? = null,
  val mappaLevel: String? = null,
  val policeForce: String? = null,
  val probationArea: String? = null,
  val recommendedToOwner: String? = null,
  val receivedDateTime: LocalDateTime? = null,
  val releaseDate: LocalDate? = null,
  val riskOfContrabandDetails: String? = null,
  val riskOfSeriousHarmLevel: String? = null,
  val sentenceDate: LocalDate? = null,
)
