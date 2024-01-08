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
  val nomsId: String,
  val prisonerCategory: String,
  val prisonNumber: String,
  val sentences: List<SentenceDetails>,
  val status: String,
  val youngOffender: String,
)

data class SentenceDetails(
  val dateOfSentence: String,
  val custodyType: String,
  val mappaLevel: String,
)