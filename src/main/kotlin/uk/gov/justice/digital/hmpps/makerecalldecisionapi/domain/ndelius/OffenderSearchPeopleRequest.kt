package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenderSearchPeopleRequest(
  val pageable: Pageable,
  val searchOptions: SearchOptions
)
