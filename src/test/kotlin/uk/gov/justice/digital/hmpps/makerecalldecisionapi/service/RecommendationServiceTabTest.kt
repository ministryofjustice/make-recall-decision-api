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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationStatusForRecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsListItem
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
// This test is broken out from RecommendationServiceTest as it's getting too big. This is specifically for tests related to the 'Recommendation tab'
internal class RecommendationServiceTabTest : ServiceTestBase() {

  @ParameterizedTest
  @MethodSource("recommendationTabTestData")
  internal fun `given a recommendation in recall considered state then return these details in the recommendation tab response`(testData: RecommendationTabTestData) {
    runTest {

      val recommendation = MrdTestDataBuilder.recommendationDataEntityData(crn, status = testData.recommendationStatus, recallTypeValue = testData.recallType)

      given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name, Status.DOCUMENT_DOWNLOADED.name)))
        .willReturn(listOf(recommendation))

      given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)))
        .willReturn(listOf(recommendation))

      val response = recommendationService.getRecommendations(crn)

      then(mockPersonDetailService).should().buildPersonalDetailsOverviewResponse(crn)

      assertThat(
        response,
        equalTo(
          RecommendationsResponse(
            null,
            null,
            listOf(expectedRecommendationListItemResponse(testData.recommendationTabStatus)),
            expectedActiveRecommendationResponse(testData.recommendationStatus, testData.recallType)
          )
        )
      )
    }
  }

  private fun expectedRecommendationListItemResponse(recommendationTabStatus: RecommendationStatusForRecallType): RecommendationsListItem {
    return RecommendationsListItem(
      recommendationId = 1,
      statusForRecallType = recommendationTabStatus,
      lastModifiedByName = "jack",
      createdDate = "2022-07-01T15:22:24.567Z",
      lastModifiedDate = "2022-07-01T15:22:24.567Z",
    )
  }

  private fun expectedActiveRecommendationResponse(status: Status, recallTypeValue: RecallTypeValue?): ActiveRecommendation {

    val recallType = if (recallTypeValue != null) {
      RecallType(
        selected = RecallTypeSelectedValue(
          value = recallTypeValue,
          details = "My details"
        ),
        allOptions = listOf(
          TextValueOption(value = "NO_RECALL", text = "No recall"),
          TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
          TextValueOption(value = "STANDARD", text = "Standard")
        )
      )
    } else {
      null
    }

    return ActiveRecommendation(
      recommendationId = 1,
      lastModifiedDate = "2022-07-01T15:22:24.567Z",
      lastModifiedBy = "Jack",
      lastModifiedByName = "jack",
      recallType = recallType,
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
        RecommendationTabTestData(Status.RECALL_CONSIDERED, RecommendationStatusForRecallType.CONSIDERING_RECALL, null),
        RecommendationTabTestData(Status.DRAFT, RecommendationStatusForRecallType.MAKING_DECISION_NOT_TO_RECALL, RecallTypeValue.NO_RECALL),
        RecommendationTabTestData(Status.DRAFT, RecommendationStatusForRecallType.MAKING_DECISION_TO_RECALL, RecallTypeValue.STANDARD),
        RecommendationTabTestData(Status.DRAFT, RecommendationStatusForRecallType.RECOMMENDATION_STARTED, null),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, RecommendationStatusForRecallType.DECIDED_NOT_TO_RECALL, RecallTypeValue.NO_RECALL),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, RecommendationStatusForRecallType.DECIDED_TO_RECALL, RecallTypeValue.FIXED_TERM),
        RecommendationTabTestData(Status.DOCUMENT_DOWNLOADED, RecommendationStatusForRecallType.UNKNOWN, null)
      )
  }

  data class RecommendationTabTestData(
    val recommendationStatus: Status,
    val recommendationTabStatus: RecommendationStatusForRecallType,
    val recallType: RecallTypeValue?
  )
}
