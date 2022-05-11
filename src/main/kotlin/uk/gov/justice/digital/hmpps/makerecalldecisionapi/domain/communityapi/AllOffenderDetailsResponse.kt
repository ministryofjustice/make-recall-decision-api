package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AllOffenderDetailsResponse(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val surname: String? = null,
  val gender: String? = null,
  val contactDetails: ContactDetails?,
  val offenderManagers: List<OffenderManager>?
)

data class OffenderManager(
  val active: Boolean?,
  val trustOfficer: TrustOfficer?,
  val staff: Staff?,
  val providerEmployee: ProviderEmployee?,
  val team: Team?
)

data class Team(
  val telephone: String?,
  val emailAddress: String?,
  val code: String?,
  val description: String?
)

data class TrustOfficer(
  val forenames: String?,
  val surname: String?
)

data class Staff(
  val forenames: String?,
  val surname: String?
)

data class ProviderEmployee(
  val forenames: String?,
  val surname: String?
)

data class AddressStatus(
  val code: String?,
  val description: String?
)

data class ContactDetails(
  val addresses: List<Address>?
)

data class Address(
  val town: String? = null,
  val county: String? = null,
  val district: String? = null,
  val status: AddressStatus? = null,
  val postcode: String? = null,
  val addressNumber: String? = null,
  val buildingName: String? = null
)
