package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecFlowEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toRecFlowEventModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecFlowEventEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecFlowEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecFlowEventRepository

@Transactional
@Service
internal class RecFlowEventService(
  val recFlowRepository: RecFlowEventRepository,
) {
  suspend fun createRecFlowEvent(
    recFlowEvent: RecFlowEvent
  ): RecFlowEvent {
    val recFlowEventEntity = RecFlowEventEntity(data = recFlowEvent.toRecFlowEventModel())
    return recFlowRepository.save(recFlowEventEntity).toRecFlowEvent()
  }
}
