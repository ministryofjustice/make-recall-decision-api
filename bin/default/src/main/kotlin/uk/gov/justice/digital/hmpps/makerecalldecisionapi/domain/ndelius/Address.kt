package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class Address(
  val town: String? = null,
  val county: String? = null,
  val district: String? = null,
  val streetName: String? = null,
  val status: AddressStatus? = null,
  val postcode: String? = null,
  val addressNumber: String? = null,
  val buildingName: String? = null,
  val noFixedAbode: Boolean? = null
)
