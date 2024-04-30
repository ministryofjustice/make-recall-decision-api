package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookRecallToPpud(
  val decisionDateTime: LocalDateTime? = null,
  val custodyType: String? = null,
  val releasingPrison: String? = null,
  val indexOffence: String? = null,
  val ppudSentenceId: String? = null,
  val mappaLevel: String? = null,
  val policeForce: String? = null,
  val probationArea: String? = null,
  val receivedDateTime: LocalDateTime? = null,
  val sentenceDate: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val firstNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val cro: String? = null,
  val prisonNumber: String? = null,
  val legislationReleasedUnder: String? = null,
  val minute: String? = null,
)
