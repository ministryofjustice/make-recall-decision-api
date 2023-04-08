package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationStatusServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    DateTimeUtils.setCurrentMillisFixed(1658828907443)
  }

  @Test()
  fun `create recommendation status`() {
    runTest {
      // given
      val recommendationStatusToSave = RecommendationStatusEntity(
        recommendationId = 1L,
        createdBy = "BILL",
        createdByUserName = "Bill",
        created = "2022-07-26T09:48:27.443Z",
        active = true,
        status = "NEW_STATUS"
      )

      // and
      given(recommendationStatusRepository.save(any())).willReturn(recommendationStatusToSave)

      // when
      val response = recommendationStatusService.updateRecommendationStatus(
        RecommendationStatusRequest(
          activate = "NEW_STATUS"
        ),
        recommendationId = 1L,
        readableNameOfUser = "Bill",
        userId = "BILL"
      )

      // then
      assertThat(response?.recommendationId).isNotNull
      assertThat(response?.status).isEqualTo("NEW_STATUS")
      assertThat(response?.created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response?.createdBy).isEqualTo("BILL")
      assertThat(response?.createdByUserName).isEqualTo("Bill")
      assertThat(response?.active).isEqualTo(true)
      assertThat(response?.modified).isEqualTo(null)
      assertThat(response?.modifiedBy).isEqualTo(null)
      assertThat(response?.modifiedByUserName).isEqualTo(null)

      val captor = argumentCaptor<RecommendationStatusEntity>()
      then(recommendationStatusRepository).should().save(captor.capture())
      val recommendationStatusEntity = captor.firstValue

      assertThat(recommendationStatusEntity.id).isNotNull()
      assertThat(recommendationStatusEntity.status).isEqualTo("NEW_STATUS")
      assertThat(recommendationStatusEntity.created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationStatusEntity.createdBy).isEqualTo("BILL")
      assertThat(recommendationStatusEntity.createdByUserName).isEqualTo("Bill")
      assertThat(recommendationStatusEntity.modified).isEqualTo(null)
      assertThat(recommendationStatusEntity.active).isEqualTo(true)
      assertThat(recommendationStatusEntity.modifiedBy).isEqualTo(null)
      assertThat(recommendationStatusEntity.modifiedByUserName).isEqualTo(null)
    }
  }

  @Test()
  fun `update recommendation status`() {
    runTest {
      // given
      val recommendationStatusToSave = RecommendationStatusEntity(
        recommendationId = 1L,
        createdBy = "BILL",
        createdByUserName = "Bill",
        created = "2022-07-26T09:48:27.443Z",
        active = true,
        status = "NEW_STATUS"
      )

      // and
      given(recommendationStatusRepository.findByRecommendationIdAndStatus(anyLong(), anyString()))
        .willReturn(
          listOf(recommendationStatusToSave.copy(status = "SOME_OTHER_STATUS"))
        )

      // and
      given(recommendationStatusRepository.findByRecommendationId(anyLong()))
        .willReturn(
          listOf(recommendationStatusToSave, recommendationStatusToSave.copy(status = "SOME_OTHER_STATUS", active = false, modified = "2022-07-26T09:48:27.443Z", modifiedBy = "BILL", modifiedByUserName = "Bill"))
        )

      // and
      given(recommendationStatusRepository.save(any())).willReturn(recommendationStatusToSave)
      given(recommendationStatusRepository.saveAll(anyList())).willReturn(listOf(recommendationStatusToSave.copy(status = "SOME_OTHER_STATUS", active = false, modified = "2022-07-26T09:48:27.443Z", modifiedBy = "BILL", modifiedByUserName = "Bill")))

      // and
      recommendationStatusService.updateRecommendationStatus(
        RecommendationStatusRequest(
          activate = "NEW_STATUS",
          deActivate = "SOME_OTHER_STATUS"
        ),
        recommendationId = 1L,
        readableNameOfUser = "Bill",
        userId = "BILL"
      )

      // when
      val response = recommendationStatusService.fetchRecommendationStatuses(1L)

      // then
      assertThat(response[0].recommendationId).isNotNull
      assertThat(response[0].status).isEqualTo("NEW_STATUS")
      assertThat(response[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response[0].createdBy).isEqualTo("BILL")
      assertThat(response[0].createdByUserName).isEqualTo("Bill")
      assertThat(response[0].active).isEqualTo(true)
      assertThat(response[0].modified).isEqualTo(null)
      assertThat(response[0].modifiedBy).isEqualTo(null)
      assertThat(response[0].modifiedByUserName).isEqualTo(null)

      // and
      assertThat(response[1].recommendationId).isNotNull
      assertThat(response[1].status).isEqualTo("SOME_OTHER_STATUS")
      assertThat(response[1].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response[1].createdBy).isEqualTo("BILL")
      assertThat(response[1].createdByUserName).isEqualTo("Bill")
      assertThat(response[1].active).isEqualTo(false)
      assertThat(response[1].modified).isEqualTo("2022-07-26T09:48:27.443Z") // FIXME BS not getting added
      assertThat(response[1].modifiedBy).isEqualTo("BILL")
      assertThat(response[1].modifiedByUserName).isEqualTo("Bill")

      val captor = argumentCaptor<RecommendationStatusEntity>()
      then(recommendationStatusRepository).should().save(captor.capture())
      val recommendationStatusEntity = captor.firstValue

      assertThat(recommendationStatusEntity.id).isNotNull()
      assertThat(recommendationStatusEntity.status).isEqualTo("NEW_STATUS")
      assertThat(recommendationStatusEntity.created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationStatusEntity.createdBy).isEqualTo("BILL")
      assertThat(recommendationStatusEntity.createdByUserName).isEqualTo("Bill")
      assertThat(recommendationStatusEntity.modified).isEqualTo(null)
      assertThat(recommendationStatusEntity.modifiedBy).isEqualTo(null)
      assertThat(recommendationStatusEntity.modifiedByUserName).isEqualTo(null)

      val listCaptor = argumentCaptor<List<RecommendationStatusEntity>>()
      then(recommendationStatusRepository).should().saveAll(listCaptor.capture())
      val recommendationStatusEntityList = listCaptor.firstValue

      assertThat(recommendationStatusEntityList[0].id).isNotNull()
      assertThat(recommendationStatusEntityList[0].status).isEqualTo("SOME_OTHER_STATUS")
      assertThat(recommendationStatusEntityList[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationStatusEntityList[0].createdBy).isEqualTo("BILL")
      assertThat(recommendationStatusEntityList[0].createdByUserName).isEqualTo("Bill")
      assertThat(recommendationStatusEntityList[0].modified).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationStatusEntityList[0].modifiedBy).isEqualTo("BILL")
      assertThat(recommendationStatusEntityList[0].modifiedByUserName).isEqualTo("Bill")
      assertThat(recommendationStatusEntityList[0].active).isEqualTo(false)
    }
  }
}
