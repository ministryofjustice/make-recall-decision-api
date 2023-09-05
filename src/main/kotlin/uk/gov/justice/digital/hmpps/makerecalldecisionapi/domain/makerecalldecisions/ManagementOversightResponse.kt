package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class ManagementOversightResponse(
  val notes: String?,
  val sensitive: Boolean? = false,
)
