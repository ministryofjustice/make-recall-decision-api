package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecFlowEventEntity

@Repository
interface RecFlowEventRepository : JpaRepository<RecFlowEventEntity, Long>
