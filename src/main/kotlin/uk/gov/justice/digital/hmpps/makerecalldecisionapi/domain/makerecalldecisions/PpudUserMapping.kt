package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity

data class PpudUserMapping(
  val id: String?, // optional, as requests to create new mappings will have no ID yet
  val userName: String,
  val ppudUserFullName: String,
  val ppudTeamName: String,
  val ppudUserName: String,
) {
  constructor(ppudUserMappingEntity: PpudUserMappingEntity) :
    this(ppudUserMappingEntity.id.toString(), ppudUserMappingEntity.userName, ppudUserMappingEntity.ppudUserFullName, ppudUserMappingEntity.ppudTeamName, ppudUserMappingEntity.ppudUserName)
}
