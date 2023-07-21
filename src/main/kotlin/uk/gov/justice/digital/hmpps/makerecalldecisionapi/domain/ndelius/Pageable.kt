package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class Pageable(
  val page: Int,
  val size: Int,
  val sort: List<String>
)
