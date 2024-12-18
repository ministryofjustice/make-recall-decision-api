package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudUserResponse(
  val results: List<PpudUser> = emptyList(),
)
