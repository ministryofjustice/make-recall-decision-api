package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class Paging(
  val page: Int = 0,
  val pageSize: Int = 0,
  val totalNumberOfPages: Int? = 0,
)
