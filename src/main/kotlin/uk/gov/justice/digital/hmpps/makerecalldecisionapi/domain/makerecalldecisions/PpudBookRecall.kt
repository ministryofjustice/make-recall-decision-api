package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate
import java.time.LocalDateTime

data class PpudBookRecall(
  val decisionDateTime: LocalDateTime? = null,
  val isInCustody: Boolean? = null,
  val mappaLevel: String? = null,
  val policeForce: String? = null,
  val probationArea: String? = null,
  val recommendedTo: PpudUser? = null,
  val receivedDateTime: LocalDateTime? = null,
  val releaseDate: LocalDate? = null,
  val riskOfContrabandDetails: String? = null,
  val riskOfSeriousHarmLevel: String? = null,
  val sentenceDate: LocalDate? = null,
)
