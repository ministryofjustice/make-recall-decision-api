package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationHistoryEntity

@Repository
interface RecommendationHistoryRepository : JpaRepository<RecommendationHistoryEntity, Long> {
  fun findByrecommendationId(@Param("crn") recommendationId: Long): List<RecommendationHistoryEntity>
}
