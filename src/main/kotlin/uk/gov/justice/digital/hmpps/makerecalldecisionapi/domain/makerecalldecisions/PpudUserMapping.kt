package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity

data class PpudUserMapping(
  val fullName: String,
  val teamName: String,
) {
  constructor(ppudUserMappingEntity: PpudUserMappingEntity) :
    this(ppudUserMappingEntity.ppudUserFullName, ppudUserMappingEntity.ppudTeamName)
}
