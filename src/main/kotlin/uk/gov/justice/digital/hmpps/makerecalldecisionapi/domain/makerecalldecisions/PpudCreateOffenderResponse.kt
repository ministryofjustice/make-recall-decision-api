package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudCreateOffenderResponse(
  val offender: PpudCreatedOffender = PpudCreatedOffender(),
)

data class PpudCreatedOffender(
  val id: String? = null,
  val sentence: PpudCreatedSentence? = null,
)

data class PpudCreatedSentence(
  val id: String? = null,
)
