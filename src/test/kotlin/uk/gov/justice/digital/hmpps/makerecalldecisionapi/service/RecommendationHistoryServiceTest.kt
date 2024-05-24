package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationHistoryEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationHistoryRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class RecommendationHistoryServiceTest {

  @Mock
  private lateinit var recommendationHistoryRepository: RecommendationHistoryRepository
  private lateinit var service: RecommendationHistoryService

  @Test
  fun `returns empty list and crn when no subject access content available`() {
    // given
    service = RecommendationHistoryService(recommendationHistoryRepository)
    given(recommendationHistoryRepository.findByCrn(any(), any(), any())).willReturn(emptyList())

    // when
    val result = service.getProbationContentFor("bla", LocalDate.now(), LocalDate.now())

    // then
    assertThat(result?.content).extracting("crn").isEqualTo("bla")
    assertThat(result?.content).extracting("recommendations").asList().isEmpty()
  }

  @Test
  fun `returns InvalidRequestException when no dates supplied`() {
    // given
    service = RecommendationHistoryService(recommendationHistoryRepository)

    // when and then
    Assertions.assertThatThrownBy {
      runTest {
        service.getProbationContentFor("bla", null, null)
      }
    }.isInstanceOf(InvalidRequestException::class.java)
      .hasMessage("Both fromDate and toDate must be present")
  }

  @Test
  fun `returns content`() {
    // given
    service = RecommendationHistoryService(recommendationHistoryRepository)
    val recommendationHistoryRecords = listOf(
      RecommendationHistoryEntity(
        recommendationId = 123L,
        recommendation = RecommendationModel(crn = "123", createdBy = "Bill"),
      ),
    )
    given(recommendationHistoryRepository.findByCrn(any(), any(), any())).willReturn(recommendationHistoryRecords)

    // when
    val result = service.getProbationContentFor("bla", LocalDate.now(), LocalDate.now())

    // then
    assertThat(result?.content).extracting("recommendationId").isEqualTo(123L)
    assertThat(result?.content).extracting("crn").isEqualTo("123")
    assertThat(result?.content).extracting("recommendations").asList().first().extracting("createdBy").isEqualTo("Bill")
  }
}
