package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import java.time.LocalDate
import java.time.LocalDateTime

data class BookRecallToPpud(
  val decisionDateTime: LocalDateTime? = null,
  val isInCustody: Boolean? = null,
  val custodyType: String? = null,
  val releasingPrison: String? = null,
  val indexOffence: String? = null,
  val ppudSentenceId: String? = null,
  val mappaLevel: String? = null,
  val policeForce: String? = null,
  val probationArea: String? = null,
  @Deprecated("Replaced by recommendedTo")
  val recommendedToOwner: String? = null,
  val recommendedTo: PpudUser? = null,
  val receivedDateTime: LocalDateTime? = null,
  val releaseDate: LocalDate? = null,
  val riskOfContrabandDetails: String? = null,
  val riskOfSeriousHarmLevel: String? = null,
  val sentenceDate: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val firstNames: String? = null,
  // deprecated - keep for now to prevent dev from breaking.
  val firstName: String? = null,
  // deprecated - keep for now to prevent dev from breaking.
  val secondName: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val cro: String? = null,
  val prisonNumber: String? = null,
  val legislationReleasedUnder: String? = null,
)
