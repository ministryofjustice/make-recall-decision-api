package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import java.time.LocalDate

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

  @Query(
    value = "SELECT t.* FROM make_recall_decision.public.recommendations t " +
      "WHERE t.data ->> 'crn' = :crn " +
      "AND t.deleted = false " +
      "AND (data ->> 'createdDate')::timestamp >= COALESCE(:fromDate, (data ->> 'createdDate')::timestamp) " +
      "AND (data ->> 'createdDate')::timestamp <= COALESCE(:toDate, (data ->> 'createdDate')::timestamp) ",
    nativeQuery = true,
  )
  fun findByCrnAndCreatedDate(
    @Param("crn") crn: String,
    @Param("fromDate") fromDate: LocalDate?,
    @Param("toDate") toDate: LocalDate?,
  ): List<RecommendationEntity>

  @Transactional
  @Modifying
  @Query(value = "UPDATE recommendations SET deleted=true WHERE id IN (:ids)", nativeQuery = true)
  fun softDeleteByIds(@Param("ids") ids: List<Long>)
}
