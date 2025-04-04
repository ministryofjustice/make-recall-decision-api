package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of EstablishmentMappingEntity
 * with their fields pre-filled with random values. Intended for use in
 * unit tests.
 */

fun establishmentMappingEntity(
  nomisAgencyId: String = randomString(),
  ppudEstablishment: String = randomString(),
) = EstablishmentMappingEntity(
  nomisAgencyId,
  ppudEstablishment,
)
