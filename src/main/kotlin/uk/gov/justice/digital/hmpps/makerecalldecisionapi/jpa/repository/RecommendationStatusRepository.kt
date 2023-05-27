package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity

@Repository
interface RecommendationStatusRepository : JpaRepository<RecommendationStatusEntity, Long> {
  fun findByRecommendationId(@Param("recommendationId") recommendationId: Long): List<RecommendationStatusEntity>
  fun findByRecommendationIdAndName(recommendationId: Long, name: String?): List<RecommendationStatusEntity>
  fun findByName(name: String?): List<RecommendationStatusEntity>
}
