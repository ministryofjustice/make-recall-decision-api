package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AllOffenderDetailsResponse(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val surname: String?,
  val contactDetails: ContactDetails?,
  val offenderManagers: List<OffenderManager>?
)

data class OffenderManager(
  val active: Boolean?,
  val trustOfficer: TrustOfficer?,
  val staff: Staff?,
  val providerEmployee: ProviderEmployee?
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
  val town: String?,
  val county: String?,
  val status: AddressStatus?
)
