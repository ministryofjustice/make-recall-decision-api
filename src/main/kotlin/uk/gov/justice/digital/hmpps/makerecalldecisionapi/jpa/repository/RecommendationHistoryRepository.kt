package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationHistoryEntity
import java.time.LocalDate

@Repository
interface RecommendationHistoryRepository : JpaRepository<RecommendationHistoryEntity, Long> {
  fun findByrecommendationId(@Param("crn") recommendationId: Long): List<RecommendationHistoryEntity>

  @Query(
    value = """
      SELECT t.* FROM make_recall_decision.public.recommendation_history t 
      WHERE t.recommendation ->> 'crn' = :crn
      AND DATE(t.modified) >= :startDate
      AND DATE(t.modified) <= :endDate
      """,
    nativeQuery = true,
  )
  fun findByCrn(
    @Param("crn") crn: String,
    @Param("startDate") startDate: LocalDate,
    @Param("endDate") endDate: LocalDate,
  ): List<RecommendationHistoryEntity>
}
