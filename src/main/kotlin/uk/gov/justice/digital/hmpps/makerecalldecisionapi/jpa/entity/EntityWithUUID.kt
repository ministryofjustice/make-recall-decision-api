package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import java.util.UUID
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class EntityWithUUID(
  @Id
  private val id: UUID = UUID.randomUUID()
)
