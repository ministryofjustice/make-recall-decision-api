package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class Pageable(
  val page: Int, // Page starts at 0
  val size: Int,
  val sort: List<String>
)
