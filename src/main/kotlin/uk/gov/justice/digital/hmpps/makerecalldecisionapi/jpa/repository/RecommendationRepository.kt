package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity

@Repository
interface RecommendationRepository : JpaRepository<RecommendationEntity, Long> {

  @Query(
    value = "SELECT t.* FROM make_recall_decision.public.recommendations t WHERE CAST(t.data ->> 'crn' AS VARCHAR) = :crn " +
      "AND CAST(t.data ->> 'status' AS VARCHAR) = :status",
    nativeQuery = true
  )
  fun findByCrnAndStatus(@Param("crn")crn: String, @Param("status")status: String): List<RecommendationEntity>
}
