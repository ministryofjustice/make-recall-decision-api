package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.io.Serializable

data class LocalPoliceContact(
  val contactName: String? = null,
  val phoneNumber: String? = null,
  val faxNumber: String? = null,
  val emailAddress: String? = null,
) : Serializable
