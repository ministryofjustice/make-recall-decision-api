package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.EventType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecFlowEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecFlowEventEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecFlowEventModel

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecFlowServiceTest : ServiceTestBase() {

  @Test
  fun `creates a new recFlowEvent in the database`() {
    runTest {
      // given
      val recFlowEventToSave = RecFlowEventEntity(
        data = RecFlowEventModel(
          crn = "12345",
          userId = "Bill",
          timeStamp = "2022-09-12T15:00:08",
          eventType = EventType.SEARCH_RESULT_CLICKED
        )
      )

      // and
      given(recFlowEventRepository.save(any())).willReturn(recFlowEventToSave)

      // when
      val response = recFlowEventService.createRecFlowEvent(
        RecFlowEvent(
          crn = "12345",
          userId = "Bill",
          timeStamp = "2022-09-12T15:00:08",
          eventType = EventType.SEARCH_RESULT_CLICKED
        )
      )

      // then
      assertThat(response.id).isNotNull
      assertThat(response.userId).isEqualTo(recFlowEventToSave.data.userId)
      assertThat(response.eventType).isEqualTo(recFlowEventToSave.data.eventType)
      assertThat(response.timeStamp).isEqualTo(recFlowEventToSave.data.timeStamp)
      assertThat(response.crn).isEqualTo(recFlowEventToSave.data.crn)

      val captor = argumentCaptor<RecFlowEventEntity>()
      then(recFlowEventRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(recommendationEntity.id).isNotNull()
      assertThat(recommendationEntity.data.crn).isEqualTo(recFlowEventToSave.data.crn)
      assertThat(recommendationEntity.data.userId).isEqualTo(recFlowEventToSave.data.userId)
      assertThat(recommendationEntity.data.timeStamp).isEqualTo(recFlowEventToSave.data.timeStamp)
    }
  }
}
