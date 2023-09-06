package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class ContactGroupResponse(
  val groupId: String,
  val label: String,
  val contactTypeCodes: List<String?>,
)
