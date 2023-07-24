package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class OffenderSearchResponse(
  val results: List<OffenderSearchOffender> = emptyList(),
  val paging: Paging = Paging()
)
