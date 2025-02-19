package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudDetailsResponse(
  val offender: OffenderDetails,
)

data class OffenderDetails(
  val id: String,
  val croOtherNumber: String,
  val dateOfBirth: String,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val immigrationStatus: String,
  val establishment: String,
  val nomsId: String,
  val prisonerCategory: String,
  val prisonNumber: String,
  val sentences: List<SentenceDetails>,
  val status: String,
  val youngOffender: String,
)

data class SentenceDetails(
  val id: String?,
  val sentenceExpiryDate: String?,
  val dateOfSentence: String?,
  val custodyType: String?,
  val mappaLevel: String?,
  val licenceExpiryDate: String?,
  val offence: OffenceDetails?,
  val releaseDate: String?,
  val sentenceLength: SentenceLength?,
  val sentencingCourt: String?,
)

data class OffenceDetails(
  val indexOffence: String?,
  val dateOfIndexOffence: String?,
)

data class SentenceLength(
  val partYears: Int?,
  val partMonths: Int?,
  val partDays: Int?,
)
