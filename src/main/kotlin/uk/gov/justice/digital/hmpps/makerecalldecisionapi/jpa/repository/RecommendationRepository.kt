package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity

@Repository
interface RecommendationRepository : JpaRepository<RecommendationEntity, Long> {

  // TODO: Add status
  @Query(value = "SELECT recommendations FROM RecommendationEntity recommendations WHERE FUNC('jsonb_extract_path_text', recommendations.data, 'crn') = :crn")
  fun findByCrnAndStatus(crn: String): List<RecommendationEntity>
}
