package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl

data class LicenceConditionSearch(
  val nomsId: List<String>,
  val status: List<String> = listOf("ACTIVE"),
)
