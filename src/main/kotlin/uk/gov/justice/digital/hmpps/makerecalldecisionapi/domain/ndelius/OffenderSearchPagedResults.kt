package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenderSearchPagedResults(
  val content: List<OffenderDetails>,
  val pageable: PageableResponse,
  val totalPages: Int?
)
