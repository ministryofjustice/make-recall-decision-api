package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.TypeDef
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
open class EntityWithId(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  open var id: Long? = null
)
