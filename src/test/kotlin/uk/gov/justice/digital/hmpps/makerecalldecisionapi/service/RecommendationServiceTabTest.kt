package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsListItem
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
// This test is broken out from RecommendationServiceTest as it's getting too big. This is specifically for tests related to the 'Recommendation tab'
internal class RecommendationServiceTabTest : ServiceTestBase() {

  @ParameterizedTest
  @MethodSource("recommendationTabTestData")
  internal fun `given a recommendation in recall considered state then return these details in the recommendation tab response`(testData: RecommendationTabTestData) {
    runTest {

      val lastModifiedDate1 = "2022-07-02T15:22:24.567Z"
      val lastModifiedDate2 = "2022-07-01T15:22:24.567Z"
      val lastModifiedDate3 = "2022-07-03T15:22:24.567Z"

      val recommendation1 = MrdTestDataBuilder.recommendationDataEntityData(crn, status = testData.recommendationStatus, recallTypeValue = testData.recallTypeValue, lastModifiedDate = lastModifiedDate1)
      val recommendation2 = MrdTestDataBuilder.recommendationDataEntityData(crn, status = testData.recommendationStatus, recallTypeValue = testData.recallTypeValue, lastModifiedDate = lastModifiedDate2)
      val recommendation3 = MrdTestDataBuilder.recommendationDataEntityData(crn, status = testData.recommendationStatus, recallTypeValue = testData.recallTypeValue, lastModifiedDate = lastModifiedDate3)

      given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name, Status.DOCUMENT_DOWNLOADED.name)))
        .willReturn(listOf(recommendation1, recommendation2, recommendation3))

      given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)))
        .willReturn(listOf(recommendation3))

      val response = recommendationService.getRecommendations(crn)

      then(mockPersonDetailService).should().buildPersonalDetailsOverviewResponse(crn)

      assertThat(
        response,
        equalTo(
          RecommendationsResponse(
            null,
            null,
            listOf(expectedRecommendationListItemResponse(recommendation3), expectedRecommendationListItemResponse(recommendation1), expectedRecommendationListItemResponse(recommendation2)),
            expectedActiveRecommendationResponse(testData.recommendationStatus, recommendation3)
          )
        )
      )
    }
  }

  private fun expectedRecommendationListItemResponse(recommendation: RecommendationEntity): RecommendationsListItem {
    return RecommendationsListItem(
      recommendationId = 1,
      lastModifiedByName = "jack",
      createdDate = "2022-07-01T15:22:24.567Z",
      lastModifiedDate = recommendation.data.lastModifiedDate,
      status = recommendation.data.status,
      recallType = recommendation.data.recallType
    )
  }

  private fun expectedActiveRecommendationResponse(status: Status, recommendation: RecommendationEntity): ActiveRecommendation {

    return ActiveRecommendation(
      recommendationId = 1,
      lastModifiedDate = recommendation.data.lastModifiedDate,
      lastModifiedBy = "Jack",
      lastModifiedByName = "jack",
      recallType = recommendation.data.recallType,
      recallConsideredList = listOf(
        RecallConsidered(
          id = 1,
          userId = "bill",
          createdDate = "2022-07-26T09:48:27.443Z",
          userName = "Bill",
          recallConsideredDetail = "I have concerns about their behaviour"
        )
      ),
      status = status,
    )
  }

  companion object {
    @JvmStatic
    private fun recommendationTabTestData(): Stream<RecommendationTabTestData> =
      Stream.of(
        RecommendationTabTestData(Status.RECALL_CONSIDERED, null),
        RecommendationTabTestData(Status.DRAFT, RecallTypeValue.NO_RECALL),
        RecommendationTabTestData(Status.DRAFT, RecallTypeValue.STANDARD),
        RecommendationTabTestData(Status.DRAFT, null),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, RecallTypeValue.NO_RECALL),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, RecallTypeValue.FIXED_TERM),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, null)
      )
  }

  data class RecommendationTabTestData(
    val recommendationStatus: Status,
    val recallTypeValue: RecallTypeValue?
  )
}
