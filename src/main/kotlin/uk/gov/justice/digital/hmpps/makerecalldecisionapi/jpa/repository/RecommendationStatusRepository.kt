package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import java.time.LocalDateTime

@Repository
interface RecommendationStatusRepository : JpaRepository<RecommendationStatusEntity, Long> {
  fun findByRecommendationId(@Param("recommendationId") recommendationId: Long): List<RecommendationStatusEntity>
  fun findByRecommendationIdAndName(recommendationId: Long, name: String?): List<RecommendationStatusEntity>
  fun findByName(name: String?): List<RecommendationStatusEntity>

  @Query(
    value = """
            SELECT DISTINCT rs.recommendation_id
            FROM make_recall_decision.public.recommendation_status rs
            LEFT OUTER JOIN make_recall_decision.public.recommendation_status rs2
                ON rs.recommendation_id = rs2.recommendation_id
                AND rs2.active = true
                AND rs2.name IN ('SENT_TO_PPCS', 'REC_CLOSED')
            LEFT OUTER JOIN make_recall_decision.public.recommendations r
                ON rs.recommendation_id = r.id
            WHERE DATE(rs.created) < :thresholdDate
                AND rs2.recommendation_id IS NULL
                AND r.deleted = false
            LIMIT 10
        """,
    nativeQuery = true,
  )
  fun findStaleRecommendations(@Param("thresholdDate") thresholdDate: LocalDateTime): List<Long>
}
