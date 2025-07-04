package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PpudCreateOrUpdateSentenceRequest(
  val custodyType: String,
  val dateOfSentence: LocalDate,
  val licenceExpiryDate: LocalDate?,
  val mappaLevel: String?,
  val releaseDate: LocalDate?,
  val sentenceLength: SentenceLength?,
  val espCustodialPeriod: PpudYearMonth? = null, // never set in UI - should we remove it and all associated code?
  val espExtendedPeriod: PpudYearMonth? = null, // never set in UI - should we remove it and all associated code?
  val sentenceExpiryDate: LocalDate?,
  val sentencingCourt: String = "",
  val sentencedUnder: String?,
)
