package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito
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
        createdByUserFullName = "Bill",
        created = "2022-07-26T09:48:27.443Z",
        active = true,
        name = "NEW_STATUS",
      )

      // and
      given(recommendationStatusRepository.saveAll(anyList())).willReturn(listOf(recommendationStatusToSave))

      // when
      val response = recommendationStatusService.updateRecommendationStatus(
        RecommendationStatusRequest(
          activate = listOf("NEW_STATUS"),
          deActivate = emptyList(),
        ),
        recommendationId = 1L,
        readableNameOfUser = "Bill",
        userId = "BILL",
      )

      // then
      assertThat(response[0].recommendationId).isNotNull
      assertThat(response[0].name).isEqualTo("NEW_STATUS")
      assertThat(response[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response[0].createdBy).isEqualTo("BILL")
      assertThat(response[0].createdByUserFullName).isEqualTo("Bill")
      assertThat(response[0].active).isEqualTo(true)
      assertThat(response[0].modified).isEqualTo(null)
      assertThat(response[0].modifiedBy).isEqualTo(null)
      assertThat(response[0].modifiedByUserFullName).isEqualTo(null)

      val captor = argumentCaptor<List<RecommendationStatusEntity>>()
      then(recommendationStatusRepository).should().saveAll(captor.capture())
      val recommendationStatusEntity = captor.firstValue

      assertThat(recommendationStatusEntity[0].id).isNotNull()
      assertThat(recommendationStatusEntity[0].name).isEqualTo("NEW_STATUS")
      assertThat(recommendationStatusEntity[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationStatusEntity[0].createdBy).isEqualTo("BILL")
      assertThat(recommendationStatusEntity[0].createdByUserFullName).isEqualTo("Bill")
      assertThat(recommendationStatusEntity[0].modified).isEqualTo(null)
      assertThat(recommendationStatusEntity[0].active).isEqualTo(true)
      assertThat(recommendationStatusEntity[0].modifiedBy).isEqualTo(null)
      assertThat(recommendationStatusEntity[0].modifiedByUserFullName).isEqualTo(null)
    }
  }

  @Test()
  fun `update recommendation status`() {
    runTest {
      // given
      val recommendationStatusToSave = RecommendationStatusEntity(
        recommendationId = 1L,
        createdBy = "BILL",
        createdByUserFullName = "Bill",
        created = "2022-07-26T09:48:27.443Z",
        active = true,
        name = "NEW_STATUS",
      )

      // and
      given(recommendationStatusRepository.findByRecommendationIdAndName(anyLong(), anyString()))
        .willReturn(
          listOf(recommendationStatusToSave.copy(name = "SOME_OTHER_STATUS")),
        )

      // and
      given(recommendationStatusRepository.findByRecommendationId(anyLong()))
        .willReturn(
          listOf(
            recommendationStatusToSave,
            recommendationStatusToSave.copy(
              name = "SOME_OTHER_STATUS",
              active = false,
              modified = "2022-07-26T09:48:27.443Z",
              modifiedBy = "BILL",
              modifiedByUserFullName = "Bill",
            ),
          ),
        )

      // and
      given(recommendationStatusRepository.saveAll(anyList())).willReturn(
        listOf(
          recommendationStatusToSave.copy(
            name = "SOME_OTHER_STATUS",
            active = false,
            modified = "2022-07-26T09:48:27.443Z",
            modifiedBy = "BILL",
            modifiedByUserFullName = "Bill",
          ),
        ),
      )

      // and
      recommendationStatusService.updateRecommendationStatus(
        RecommendationStatusRequest(
          activate = listOf("NEW_STATUS"),
          deActivate = listOf("SOME_OTHER_STATUS"),
        ),
        recommendationId = 1L,
        readableNameOfUser = "Bill",
        userId = "BILL",
      )

      // when
      val response = recommendationStatusService.fetchRecommendationStatuses(1L)

      // then
      assertThat(response[0].recommendationId).isNotNull
      assertThat(response[0].name).isEqualTo("NEW_STATUS")
      assertThat(response[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response[0].createdBy).isEqualTo("BILL")
      assertThat(response[0].createdByUserFullName).isEqualTo("Bill")
      assertThat(response[0].active).isEqualTo(true)
      assertThat(response[0].modified).isEqualTo(null)
      assertThat(response[0].modifiedBy).isEqualTo(null)
      assertThat(response[0].modifiedByUserFullName).isEqualTo(null)

      // and
      assertThat(response[1].recommendationId).isNotNull
      assertThat(response[1].name).isEqualTo("SOME_OTHER_STATUS")
      assertThat(response[1].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(response[1].createdBy).isEqualTo("BILL")
      assertThat(response[1].createdByUserFullName).isEqualTo("Bill")
      assertThat(response[1].active).isEqualTo(false)
      assertThat(response[1].modified).isEqualTo("2022-07-26T09:48:27.443Z") // FIXME BS not getting added
      assertThat(response[1].modifiedBy).isEqualTo("BILL")
      assertThat(response[1].modifiedByUserFullName).isEqualTo("Bill")

      val captor = argumentCaptor<List<RecommendationStatusEntity>>()
      then(recommendationStatusRepository).should(BDDMockito.times(2)).saveAll(captor.capture())
      val someOtherStatus = captor.firstValue

      assertThat(someOtherStatus[0].id).isNotNull()
      assertThat(someOtherStatus[0].name).isEqualTo("SOME_OTHER_STATUS")
      assertThat(someOtherStatus[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(someOtherStatus[0].createdBy).isEqualTo("BILL")
      assertThat(someOtherStatus[0].createdByUserFullName).isEqualTo("Bill")
      assertThat(someOtherStatus[0].modified).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(someOtherStatus[0].modifiedBy).isEqualTo("BILL")
      assertThat(someOtherStatus[0].modifiedByUserFullName).isEqualTo("Bill")
      assertThat(someOtherStatus[0].active).isEqualTo(false)

      val newStatus = captor.secondValue
      assertThat(newStatus[0].id).isNotNull()
      assertThat(newStatus[0].name).isEqualTo("NEW_STATUS")
      assertThat(newStatus[0].created).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(newStatus[0].createdBy).isEqualTo("BILL")
      assertThat(newStatus[0].createdByUserFullName).isEqualTo("Bill")
      assertThat(newStatus[0].modified).isEqualTo(null)
      assertThat(newStatus[0].modifiedBy).isEqualTo(null)
      assertThat(newStatus[0].modifiedByUserFullName).isEqualTo(null)
    }
  }
}
