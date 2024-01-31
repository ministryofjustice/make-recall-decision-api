package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity

@Repository
interface RecommendationRepository : JpaRepository<RecommendationEntity, Long> {

  @Query(
    value = "SELECT t.* FROM make_recall_decision.public.recommendations t WHERE t.data ->> 'crn' = :crn " +
      "AND t.data ->> 'status' IN (:statuses) AND t.deleted=false",
    nativeQuery = true,
  )
  fun findByCrnAndStatus(
    @Param("crn") crn: String,
    @Param("statuses") statuses: List<String>,
  ): List<RecommendationEntity>

  @Query(
    value = "SELECT t.* FROM make_recall_decision.public.recommendations t WHERE t.data ->> 'crn' = :crn AND t.deleted = false",
    nativeQuery = true,
  )
  fun findByCrn(@Param("crn") crn: String): List<RecommendationEntity>
}
