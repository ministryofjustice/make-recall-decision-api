package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    DateTimeUtils.setCurrentMillisFixed(1658828907443)
  }

  @Test
  fun `creates a new recommendation in the database`() {
    // given
    val recommendationToSave = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        lastModifiedBy = "Bill",
        personName = "John Smith"
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
    assertThat(result.personName).isEqualTo("John Smith")

    then(recommendationRepository).should().save(
      recommendationToSave.copy(
        id = null,
        data = (RecommendationModel(crn = crn, status = Status.DRAFT, personName = "John Smith", lastModifiedBy = "Bill", lastModifiedDate = "2022-07-26T09:48:27.443Z", createdBy = "Bill", createdDate = "2022-07-26T09:48:27.443Z"))
      )
    )
  }

  @Test
  fun `updates a recommendation to the database`() {
    // given
    val existingRecommendation = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        personName = "John Smith",
        lastModifiedBy = "Jack",
        lastModifiedDate = "2022-07-01T15:22:24.567Z",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z"
      )
    )

    // and
    val updateRecommendationRequest = UpdateRecommendationRequest(
      status = null,
      recallType = RecallType(
        value = "NO_RECALL",
        options = listOf(
          TextValueOption(value = "NO_RECALL", text = "No recall"),
          TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
          TextValueOption(value = "STANDARD", text = "Standard")
        )
      ),
      custodyStatus = CustodyStatus(
        value = CustodyStatusValue.YES_PRISON,
        options = listOf(
          TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
          TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
          TextValueOption(value = "NO", text = "No")
        )
      )
    )

    // and
    val recommendationToSave =
      existingRecommendation.copy(
        id = existingRecommendation.id,
        data = RecommendationModel(
          crn = existingRecommendation.data.crn,
          personName = "John Smith",
          recallType = RecallType(
            value = "NO_RECALL",
            options = listOf(
              TextValueOption(value = "NO_RECALL", text = "No recall"),
              TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
              TextValueOption(value = "STANDARD", text = "Standard")
            )
          ),
          custodyStatus = CustodyStatus(
            value = CustodyStatusValue.YES_PRISON,
            options = listOf(
              TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
              TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
              TextValueOption(value = "NO", text = "No")
            )
          ),
          status = existingRecommendation.data.status,
          lastModifiedDate = "2022-07-26T09:48:27.443Z",
          lastModifiedBy = "Bill",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z"
        )
      )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // and
    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    // when
    val updateRecommendationResponse = recommendationService.updateRecommendation(updateRecommendationRequest, 1L, "Bill")

    // then
    assertThat(updateRecommendationResponse.id).isEqualTo(1)
    assertThat(updateRecommendationResponse.status).isEqualTo(Status.DRAFT)
    assertThat(updateRecommendationResponse.crn).isEqualTo(crn)
    assertThat(updateRecommendationResponse.personName).isEqualTo("John Smith")
    assertThat(updateRecommendationResponse.recallType?.value).isEqualTo("NO_RECALL")
    assertThat(updateRecommendationResponse.recallType?.options!![0].value).isEqualTo("NO_RECALL")
    assertThat(updateRecommendationResponse.recallType?.options!![0].text).isEqualTo("No recall")
    assertThat(updateRecommendationResponse.recallType?.options!![1].value).isEqualTo("FIXED_TERM")
    assertThat(updateRecommendationResponse.recallType?.options!![1].text).isEqualTo("Fixed term")
    assertThat(updateRecommendationResponse.recallType?.options!![2].value).isEqualTo("STANDARD")
    assertThat(updateRecommendationResponse.recallType?.options!![2].text).isEqualTo("Standard")
    assertThat(updateRecommendationResponse.custodyStatus?.value).isEqualTo(CustodyStatusValue.YES_PRISON)
    assertThat(updateRecommendationResponse.custodyStatus?.options!![0].value).isEqualTo("YES_PRISON")
    assertThat(updateRecommendationResponse.custodyStatus?.options!![0].text).isEqualTo("Yes, prison custody")
    assertThat(updateRecommendationResponse.custodyStatus?.options!![1].value).isEqualTo("YES_POLICE")
    assertThat(updateRecommendationResponse.custodyStatus?.options!![1].text).isEqualTo("Yes, police custody")
    assertThat(updateRecommendationResponse.custodyStatus?.options!![2].value).isEqualTo("NO")
    assertThat(updateRecommendationResponse.custodyStatus?.options!![2].text).isEqualTo("No")

    then(recommendationRepository).should().save(recommendationToSave)
    then(recommendationRepository).should().findById(1)
  }

  @Test
  fun `updates a recommendation to the database when optional fields not present on request`() {
    // given
    val existingRecommendation = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        personName = null,
        lastModifiedBy = "Bill",
        recallType = RecallType(
          value = null,
          options = null
        ),
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z"
      )
    )

    // and
    val updateRecommendationRequest = UpdateRecommendationRequest(
      status = null,
      recallType = null,
      custodyStatus = null
    )

    // and
    val recommendationToSave =
      existingRecommendation.copy(
        id = existingRecommendation.id,
        data = RecommendationModel(
          crn = existingRecommendation.data.crn,
          personName = null,
          recallType = RecallType(
            value = null,
            options = null
          ),
          status = existingRecommendation.data.status,
          lastModifiedDate = "2022-07-26T09:48:27.443Z",
          lastModifiedBy = "Bill",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z"
        )
      )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // and
    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    // when
    val updateRecommendationResponse = recommendationService.updateRecommendation(updateRecommendationRequest, 1L, "Bill")

    // then
    assertThat(updateRecommendationResponse.id).isEqualTo(1)
    assertThat(updateRecommendationResponse.status).isEqualTo(Status.DRAFT)
    assertThat(updateRecommendationResponse.crn).isEqualTo(crn)
    assertThat(updateRecommendationResponse.personName).isEqualTo(null)
    assertThat(updateRecommendationResponse.recallType?.value).isNull()
    assertThat(updateRecommendationResponse.recallType?.options).isNull()

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
            recallType = null,
            custodyStatus = null
          ),
          recommendationId = 456L,
          "Bill"
        )
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }

  @Test
  fun `get a recommendation from the database`() {
    val recallType = RecallType(
      value = "FIXED_TERM",
      options = listOf(
        TextValueOption(value = "NO_RECALL", text = "No recall"),
        TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
        TextValueOption(value = "STANDARD", text = "Standard")
      )
    )

    val custodyStatus = CustodyStatus(
      value = CustodyStatusValue.YES_PRISON,
      options = listOf(
        TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
        TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
        TextValueOption(value = "NO", text = "No")
      )
    )

    val recommendation = Optional.of(
      RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
          status = Status.DRAFT,
          personName = "John Smith",
          custodyStatus = custodyStatus,
          recallType = recallType,
          lastModifiedBy = "Bill",
          lastModifiedDate = "2022-05-18T19:33:56"
        )
      )
    )

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val recommendationResponse = recommendationService.getRecommendation(456L)

    assertThat(recommendationResponse.id).isEqualTo(recommendation.get().id)
    assertThat(recommendationResponse.crn).isEqualTo(recommendation.get().data.crn)
    assertThat(recommendationResponse.personName).isEqualTo(recommendation.get().data.personName)
    assertThat(recommendationResponse.status).isEqualTo(recommendation.get().data.status)
    assertThat(recommendationResponse.recallType?.value).isEqualTo("FIXED_TERM")
    assertThat(recommendationResponse.recallType?.options!![0].value).isEqualTo("NO_RECALL")
    assertThat(recommendationResponse.recallType?.options!![0].text).isEqualTo("No recall")
    assertThat(recommendationResponse.recallType?.options!![1].value).isEqualTo("FIXED_TERM")
    assertThat(recommendationResponse.recallType?.options!![1].text).isEqualTo("Fixed term")
    assertThat(recommendationResponse.recallType?.options!![2].value).isEqualTo("STANDARD")
    assertThat(recommendationResponse.recallType?.options!![2].text).isEqualTo("Standard")
    assertThat(recommendationResponse.custodyStatus?.value).isEqualTo(CustodyStatusValue.YES_PRISON)
    assertThat(recommendationResponse.custodyStatus?.options!![0].value).isEqualTo("YES_PRISON")
    assertThat(recommendationResponse.custodyStatus?.options!![0].text).isEqualTo("Yes, prison custody")
    assertThat(recommendationResponse.custodyStatus?.options!![1].value).isEqualTo("YES_POLICE")
    assertThat(recommendationResponse.custodyStatus?.options!![1].text).isEqualTo("Yes, police custody")
    assertThat(recommendationResponse.custodyStatus?.options!![2].value).isEqualTo("NO")
    assertThat(recommendationResponse.custodyStatus?.options!![2].text).isEqualTo("No")
  }

  @Test
  fun `get a draft recommendation for CRN from the database`() {
    val recommendation = RecommendationEntity(
      id = 1,
      data = RecommendationModel(crn = crn, lastModifiedBy = "John Smith", lastModifiedDate = "2022-07-19T12:00:00", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )

    given(recommendationRepository.findByCrnAndStatus(crn, Status.DRAFT.name))
      .willReturn(listOf(recommendation))

    val result = recommendationService.getDraftRecommendationForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation.data.lastModifiedDate)
  }

  @Test
  fun `get the latest draft recommendation for CRN when multiple draft recommendations exist in database`() {
    val recommendation1 = RecommendationEntity(
      id = 1,
      data = RecommendationModel(crn = crn, lastModifiedBy = "John Smith", lastModifiedDate = "2022-07-19T23:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation2 = RecommendationEntity(
      id = 2,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T10:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation3 = RecommendationEntity(
      id = 3,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T11:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation4 = RecommendationEntity(
      id = 4,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T09:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation5 = RecommendationEntity(
      id = 5,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Harry Winks", lastModifiedDate = "2022-07-26T12:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )

    given(recommendationRepository.findByCrnAndStatus(crn, Status.DRAFT.name))
      .willReturn(listOf(recommendation1, recommendation2, recommendation3, recommendation4, recommendation5))

    val result = recommendationService.getDraftRecommendationForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation3.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation3.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation3.data.lastModifiedDate)
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
