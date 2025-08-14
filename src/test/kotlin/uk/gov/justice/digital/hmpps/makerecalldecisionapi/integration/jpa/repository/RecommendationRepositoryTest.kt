package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendationstatus.RecommendationStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.lang.Thread.sleep
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test-repositories")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecommendationRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var repository: RecommendationRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  private val thresholdDatetime = ZonedDateTime.now(ZoneOffset.UTC)

  @Test
  fun `find all active recommendations not yet created`() {
    // given
    val activeRecommendationCreatedBeforeTheThresholdDate = createDefaultActiveRecommendation()

    val activeRecommendationCreatedAfterTheThresholdDate =
      createDefaultRecommendation(false, false, thresholdDatetime.plusDays(2))

    val expectedRecommendationIds =
      listOf(activeRecommendationCreatedAfterTheThresholdDate, activeRecommendationCreatedBeforeTheThresholdDate)
        .map { it.id }

    val recommendationWithRecClosedStatus =
      createRecommendationWithActivePreThresholdStatus(RecommendationStatus.REC_CLOSED)

    val recommendationWithRecDeletedStatus =
      createRecommendationWithActivePreThresholdStatus(RecommendationStatus.REC_DELETED)

    val recommendationWithDeletedStatus =
      createRecommendationWithActivePreThresholdStatus(RecommendationStatus.DELETED)

    val recommendationWithDataDeletedFlag =
      createDefaultRecommendation(dataDeletedFlag = true, recommendationDeletedFlag = false)

    val recommendationWithDeletedFlag =
      createDefaultRecommendation(dataDeletedFlag = false, recommendationDeletedFlag = true)

    val activeRecommendationAlreadyDownloadedBeforeTheThresholdDateTime =
      createRecommendationWithActivePreThresholdStatus(RecommendationStatus.PP_DOCUMENT_CREATED)

    val activeRecommendationAlreadyDownloadedAfterTheThresholdDateTime =
      createRecommendationWithActiveStatus(
        RecommendationStatus.PP_DOCUMENT_CREATED,
        thresholdDatetime.plusDays(2),
      )

    val otherRecommendationIds = listOf(
      recommendationWithRecClosedStatus, recommendationWithRecDeletedStatus,
      recommendationWithDeletedStatus, recommendationWithDataDeletedFlag, recommendationWithDeletedFlag,
      activeRecommendationAlreadyDownloadedBeforeTheThresholdDateTime,
      activeRecommendationAlreadyDownloadedAfterTheThresholdDateTime,
    )

    sleep(1000L * 30)

    // when
    val actualRecommendationIds = repository.findRecommendationsNotYetDownloaded(thresholdDatetime)

    // then
    assertThat(actualRecommendationIds).containsExactlyInAnyOrderElementsOf(expectedRecommendationIds)
  }

  private fun createRecommendationWithActivePreThresholdStatus(recommendationStatus: RecommendationStatus): RecommendationEntity {
    return createRecommendationWithActiveStatus(recommendationStatus, thresholdDatetime.minusDays(2))
  }

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

  private fun createDefaultActiveRecommendation(): RecommendationEntity {
    return createDefaultRecommendation(false, false)
  }

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

    entityManager.persist(recommendation)

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

    entityManager.persist(recommendationStatus)
  }
}