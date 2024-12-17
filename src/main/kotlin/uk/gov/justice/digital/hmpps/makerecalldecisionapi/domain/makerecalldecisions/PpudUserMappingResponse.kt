package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity

data class PpudUserMappingResponse(
  val ppudUserMapping: PpudUserMappingEntity?,
)
