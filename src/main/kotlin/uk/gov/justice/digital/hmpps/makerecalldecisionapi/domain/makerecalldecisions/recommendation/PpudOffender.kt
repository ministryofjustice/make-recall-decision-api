package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class PpudOffender(
  val id: String,
  val croOtherNumber: String,
  val dateOfBirth: String,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val immigrationStatus: String,
  val nomsId: String,
  val prisonerCategory: String,
  val prisonNumber: String,
  val sentences: List<PpudSentence>,
  val status: String,
  val youngOffender: String,
)

data class PpudSentence(
  val id: String?,
  //deprecated
  val offenceDescription: String?,
  val sentenceExpiryDate: String?,
  val dateOfSentence: String,
  val custodyType: String,
  val mappaLevel: String,
  val licenceExpiryDate: String?,
  val offence: PpudOffence?,
  val releases: List<PpudRelease>?,
  val sentenceLength: PpudSentenceLength?,
  val sentencingCourt: String?,
)

data class PpudOffence(
  val indexOffence: String?,
  val dateOfIndexOffence: String?,
)

data class PpudRelease(
  val category: String?,
  val dateOfRelease: String?,
  val releasedFrom: String?,
  val releasedUnder: String?,
  val releaseType: String?,
)

data class PpudSentenceLength(
  val partYears: Int?,
  val partMonths: Int?,
  val partDays: Int?,
)
