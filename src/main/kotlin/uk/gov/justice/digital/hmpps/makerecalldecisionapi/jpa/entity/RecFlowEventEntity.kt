package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.EventType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecFlowEvent
import java.security.SecureRandom
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "recflowevents")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class RecFlowEventEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  var data: RecFlowEventModel
)
data class RecFlowEventModel(
  val crn: String?,
  val userId: String?,
  val timeStamp: String?,
  val eventType: EventType?
)
fun RecFlowEventEntity.toRecFlowEvent(): RecFlowEvent {
  return RecFlowEvent(
    id = id.toString(),
    crn = data.crn,
    userId = data.userId,
    timeStamp = data.timeStamp,
    eventType = data.eventType
  )
}
