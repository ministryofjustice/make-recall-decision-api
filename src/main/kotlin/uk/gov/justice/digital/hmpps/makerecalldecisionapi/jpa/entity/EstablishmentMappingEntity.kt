package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "nomis_to_ppud_establishment_mapping")
data class EstablishmentMappingEntity(
  @Id
  val nomisAgencyId: String,
  val ppudEstablishment: String,
)
