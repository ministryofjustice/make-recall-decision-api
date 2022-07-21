package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecallTypeOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    recommendationService = RecommendationService(recommendationRepository)
  }

  @Test
  fun `saves a recommendation to the database`() {
    // given
    val crn = "12345"
    val recommendationToSave = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        lastModifiedBy = "Bill"
      )
    )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // when
    val result = recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")

    // then
    assertThat(result.id).isEqualTo(1)
    assertThat(result.status).isEqualTo(Status.DRAFT)
    assertThat(result.activeRecommendation?.recommendationId).isEqualTo(1)
    assertThat(result.activeRecommendation!!.lastModifiedBy).isEqualTo("Bill")
    assertThat(result.activeRecommendation?.lastModifiedDate).isNotNull

    then(recommendationRepository).should().save(
      recommendationToSave.copy(id = null, data = (RecommendationModel(crn = crn, status = Status.DRAFT, lastModifiedBy = "Bill", lastModifiedDate = result.activeRecommendation?.lastModifiedDate)))
    )
  }

  @Test
  fun `updates a recommendation to the database`() {
    // given
    val crn = "12345"
    val existingRecommendation = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        lastModifiedBy = "Bill"
      )
    )

    // and
    val updateRecommendationRequest = UpdateRecommendationRequest(
      status = null,
      recallType = RecallType(
        value = Recommendation.NO_RECALL,
        options = listOf(
          RecallTypeOption(value = Recommendation.NO_RECALL.name, text = Recommendation.NO_RECALL.text),
          RecallTypeOption(value = Recommendation.FIXED_TERM.name, text = Recommendation.FIXED_TERM.text),
          RecallTypeOption(value = Recommendation.STANDARD.name, text = Recommendation.STANDARD.text)
        )
      )
    )

    // and
    val recommendationToSave =
      existingRecommendation.copy(
        id = existingRecommendation.id,
        data = RecommendationModel(
          crn = existingRecommendation.data.crn,
          recommendation = updateRecommendationRequest.recallType?.value,
          status = existingRecommendation.data.status,
          lastModifiedDate = existingRecommendation.data.lastModifiedDate,
          lastModifiedBy = existingRecommendation.data.lastModifiedBy
        )
      )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // and
    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    // when
    val updateRecommendationResponse = recommendationService.updateRecommendation(updateRecommendationRequest, 1L)

    // then
    assertThat(updateRecommendationResponse.id).isEqualTo(1)
    assertThat(updateRecommendationResponse.status).isEqualTo(Status.DRAFT)
    assertThat(updateRecommendationResponse.crn).isEqualTo(crn)
    assertThat(updateRecommendationResponse.recallType?.value).isEqualTo(Recommendation.NO_RECALL)
    assertThat(updateRecommendationResponse.recallType?.options!![0].value).isEqualTo(Recommendation.NO_RECALL.name)
    assertThat(updateRecommendationResponse.recallType?.options!![0].text).isEqualTo(Recommendation.NO_RECALL.text)
    assertThat(updateRecommendationResponse.recallType?.options!![1].value).isEqualTo(Recommendation.FIXED_TERM.name)
    assertThat(updateRecommendationResponse.recallType?.options!![1].text).isEqualTo(Recommendation.FIXED_TERM.text)
    assertThat(updateRecommendationResponse.recallType?.options!![2].value).isEqualTo(Recommendation.STANDARD.name)
    assertThat(updateRecommendationResponse.recallType?.options!![2].text).isEqualTo(Recommendation.STANDARD.text)
    // TODO add once MRD-342 complete
//    assertThat(result.activeRecommendation?.recommendationId).isEqualTo(1)
//    assertThat(result.activeRecommendation?.lastModifiedBy).isEqualTo("Bill")
//    assertThat(result.activeRecommendation?.lastModifiedDate).isNotNull

    then(recommendationRepository).should().save(recommendationToSave)
    then(recommendationRepository).should().findById(1)
  }

  @Test
  fun `updates a recommendation to the database when optional fields not present on request`() {
    // given
    val crn = "12345"
    val existingRecommendation = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        lastModifiedBy = "Bill",
        recommendation = Recommendation.NO_RECALL
      )
    )

    // and
    val updateRecommendationRequest = UpdateRecommendationRequest(
      status = null,
      recallType = null
    )

    // and
    val recommendationToSave =
      existingRecommendation.copy(
        id = existingRecommendation.id,
        data = RecommendationModel(
          crn = existingRecommendation.data.crn,
          recommendation = existingRecommendation.data.recommendation,
          status = existingRecommendation.data.status,
          lastModifiedDate = existingRecommendation.data.lastModifiedDate,
          lastModifiedBy = existingRecommendation.data.lastModifiedBy
        )
      )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // and
    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    // when
    val updateRecommendationResponse = recommendationService.updateRecommendation(updateRecommendationRequest, 1L)

    // then
    assertThat(updateRecommendationResponse.id).isEqualTo(1)
    assertThat(updateRecommendationResponse.status).isEqualTo(Status.DRAFT)
    assertThat(updateRecommendationResponse.crn).isEqualTo(crn)
    assertThat(updateRecommendationResponse.recallType?.value).isEqualTo(Recommendation.NO_RECALL)
    assertThat(updateRecommendationResponse.recallType?.options!![0].value).isEqualTo(Recommendation.NO_RECALL.name)
    assertThat(updateRecommendationResponse.recallType?.options!![0].text).isEqualTo(Recommendation.NO_RECALL.text)
    assertThat(updateRecommendationResponse.recallType?.options!![1].value).isEqualTo(Recommendation.FIXED_TERM.name)
    assertThat(updateRecommendationResponse.recallType?.options!![1].text).isEqualTo(Recommendation.FIXED_TERM.text)
    assertThat(updateRecommendationResponse.recallType?.options!![2].value).isEqualTo(Recommendation.STANDARD.name)
    assertThat(updateRecommendationResponse.recallType?.options!![2].text).isEqualTo(Recommendation.STANDARD.text)
    // TODO add once MRD-342 complete
//    assertThat(result.activeRecommendation?.recommendationId).isEqualTo(1)
//    assertThat(result.activeRecommendation?.lastModifiedBy).isEqualTo("Bill")
//    assertThat(result.activeRecommendation?.lastModifiedDate).isNotNull

    then(recommendationRepository).should().save(recommendationToSave)
    then(recommendationRepository).should().findById(1)
  }

  @Test
  fun `throws exception when no recommendation available for given id on an update`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.updateRecommendation(
          UpdateRecommendationRequest(
            status = null,
            recallType = null
          ),
          recommendationId = 456L
        )
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }

  @Test
  fun `get a recommendation from the database`() {
    val recommendation = Optional.of(RecommendationEntity(data = RecommendationModel(crn = "12345")))

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val result = recommendationService.getRecommendation(456L)

    assertThat(result.id).isEqualTo(recommendation.get().id)
    assertThat(result.crn).isEqualTo(recommendation.get().data.crn)
  }

  @Test
  fun `get a draft recommendation for CRN from the database`() {
    val recommendation = RecommendationEntity(id = 1, data = RecommendationModel(crn = "X12345", lastModifiedBy = "John Smith", lastModifiedDate = "2022-07-19T12:00:00"))

    given(recommendationRepository.findByCrnAndStatus("X12345", Status.DRAFT.name))
      .willReturn(listOf(recommendation))

    val result = recommendationService.getDraftRecommendationForCrn("X12345")

    assertThat(result?.recommendationId).isEqualTo(recommendation.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation.data.lastModifiedDate)
  }

  @Test
  fun `throws exception when no recommendation available for given id`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.getRecommendation(456L)
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }
}
