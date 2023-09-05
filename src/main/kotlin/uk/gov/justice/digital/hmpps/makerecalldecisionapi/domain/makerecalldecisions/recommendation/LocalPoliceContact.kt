package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class LocalPoliceContact(
  val contactName: String? = null,
  val phoneNumber: String? = null,
  val faxNumber: String? = null,
  val emailAddress: String? = null,
)
