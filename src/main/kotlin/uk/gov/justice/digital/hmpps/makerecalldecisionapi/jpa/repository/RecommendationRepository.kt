package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import java.util.UUID

@Repository
interface RecommendationRepository : CrudRepository<RecommendationEntity, UUID>
