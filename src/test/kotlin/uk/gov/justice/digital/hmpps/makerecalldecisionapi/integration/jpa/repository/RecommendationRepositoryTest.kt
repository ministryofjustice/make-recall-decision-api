package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendationstatus.RecommendationStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
class RecommendationRepositoryTest : IntegrationTestBase() {

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  private val thresholdDatetime = ZonedDateTime.now(ZoneOffset.UTC)

  @BeforeEach
  fun clearDatabase() {
    repository.deleteAll()
    statusRepository.deleteAll()
  }

  @Test
  fun `find all active recommendations not yet created`() {
    // given
    val expectedRecommendationIds = createRecommendationsExpectedToBeSoftDeleted()
    createRecommendationsExpectedToRemainUntouched()

    // when
    val actualRecommendationIds =
      repository.findActiveRecommendationsNotYetDownloaded(thresholdDatetime.minusDays(21), thresholdDatetime)

    // then
    assertThat(actualRecommendationIds).containsExactlyInAnyOrderElementsOf(expectedRecommendationIds)
  }

  private fun createRecommendationsExpectedToBeSoftDeleted(): List<Long> {
    val activeRecommendationCreatedBeforeTheThresholdDate = createDefaultActiveRecommendation()

    return listOf(activeRecommendationCreatedBeforeTheThresholdDate.id)
  }

  private fun createRecommendationsExpectedToRemainUntouched() {
    // recommendation created before the threshold start date
    createDefaultRecommendation(false, false, thresholdDatetime.minusDays(30))

    // recommendation created after the threshold end date
    createDefaultRecommendation(false, false, thresholdDatetime.plusDays(2))

    // recommendation with REC_CLOSED status
    createRecommendationWithActivePreThresholdStatus(RecommendationStatus.REC_CLOSED)

    // recommendation with REC_DELETED status
    createRecommendationWithActivePreThresholdStatus(RecommendationStatus.REC_DELETED)

    // recommendation with DELETED status
    createRecommendationWithActivePreThresholdStatus(RecommendationStatus.DELETED)

    // recommendation with data.deleted flag
    createDefaultRecommendation(dataDeletedFlag = true, recommendationDeletedFlag = false)

    // recommendation with deleted flag
    createDefaultRecommendation(dataDeletedFlag = false, recommendationDeletedFlag = true)

    // active recommendation downloaded before the threshold datetime
    createRecommendationWithActivePreThresholdStatus(RecommendationStatus.PP_DOCUMENT_CREATED)

    // active recommendation downloaded after the threshold datetime
    createRecommendationWithActiveStatus(
      RecommendationStatus.PP_DOCUMENT_CREATED,
      thresholdDatetime.plusDays(2),
    )
  }

  private fun createRecommendationWithActivePreThresholdStatus(recommendationStatus: RecommendationStatus): RecommendationEntity = createRecommendationWithActiveStatus(recommendationStatus, thresholdDatetime.minusDays(2))

  private fun createRecommendationWithActiveStatus(
    recommendationStatus: RecommendationStatus,
    dateTime: ZonedDateTime,
  ): RecommendationEntity {
    val recommendation = createDefaultActiveRecommendation()

    createRecommendationStatusEntity(
      recommendationId = recommendation.id,
      creationDateTime = dateTime,
      name = recommendationStatus.name,
    )

    return recommendation
  }

  private fun createDefaultActiveRecommendation(): RecommendationEntity = createDefaultRecommendation(false, false)

  private fun createDefaultRecommendation(
    dataDeletedFlag: Boolean,
    recommendationDeletedFlag: Boolean,
    creationDateTime: ZonedDateTime = thresholdDatetime.minusDays(3),
  ): RecommendationEntity {
    val recommendation = RecommendationEntity(
      data = RecommendationModel(
        crn = randomString(),
        createdDate = creationDateTime.format(formatter),
        deleted = dataDeletedFlag,
      ),
      deleted = recommendationDeletedFlag,
    )

    repository.save(recommendation)

    createRecommendationStatusEntity(
      recommendationId = recommendation.id,
      creationDateTime = creationDateTime,
      name = RecommendationStatus.PO_START_RECALL.name,
    )

    return recommendation
  }

  private fun createRecommendationStatusEntity(
    recommendationId: Long,
    creationDateTime: ZonedDateTime,
    name: String,
  ) {
    val recommendationStatus = RecommendationStatusEntity(
      recommendationId = recommendationId,
      createdBy = randomString(),
      createdByUserFullName = randomString(),
      created = creationDateTime.format(formatter),
      name = name,
      active = true,
    )

    statusRepository.save(recommendationStatus)
  }
}
