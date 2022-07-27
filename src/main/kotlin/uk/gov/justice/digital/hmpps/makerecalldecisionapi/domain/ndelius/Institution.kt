package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class Institution(
  val code: String?,
  val description: String?,
  val establishmentType: EstablishmentType?,
  val institutionId: Long?,
  val institutionName: String?,
  val isEstablishment: Boolean?,
  val isPrivate: Boolean?,
  val nomsPrisonInstitutionCode: String?,
)
