package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudCreateOrUpdateSentenceRequest(
  val custodyType: String,
  val dateOfSentence: LocalDate,
  val licenceExpiryDate: LocalDate?,
  val mappaLevel: String,
  val releaseDate: LocalDate?,
  val sentenceLength: SentenceLength?,
  val espCustodialPeriod: PpudYearMonth? = null,
  val espExtendedPeriod: PpudYearMonth? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val sentencingCourt: String = "",
  val sentencedUnder: String,
)
