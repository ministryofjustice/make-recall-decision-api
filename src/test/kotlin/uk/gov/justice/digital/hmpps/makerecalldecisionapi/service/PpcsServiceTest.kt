package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPagedResults
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.PageableResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpcsServiceTest : ServiceTestBase() {

  @Test
  fun `excluded records in offender search will have blank name`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "",
              surname = "",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `single active document for ppcs`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "Harry",
              surname = "Smith",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        RecommendationEntity(
          id = 123L,
          data = RecommendationModel(crn = "X90902"),
        ),
      ),
    )

    given(recommendationStatusRepository.findByRecommendationId(123L)).willReturn(
      listOf(
        status("SENT_TO_PPCS", true, 123L),
        status("PP_DOCUMENT_CREATED", true, 123L),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    assertThat(result.results).isNotEmpty
  }

  @Test
  fun `do not return results for active recommendation if already been booked`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "Harry",
              surname = "Smith",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        RecommendationEntity(
          id = 123L,
          data = RecommendationModel(crn = "X90902"),
        ),
      ),
    )

    given(recommendationStatusRepository.findByRecommendationId(123L)).willReturn(
      listOf(
        status("ANOTHER_STATUS", true, 123L),
        status("SENT_TO_PPCS", true, 123L),
        status("REC_CLOSED", true, 123L),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `consider active recommendation only for ppcs search`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "Harry",
              surname = "Smith",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    val notTheActiveRecommendation = RecommendationEntity(
      id = 123L,
      data = RecommendationModel(crn = "X90902", lastModifiedDate = "2021-07-02T15:22:24.567Z"),
    )
    val activeRecommendation = RecommendationEntity(
      id = 1234L,
      data = RecommendationModel(crn = "X90902", lastModifiedDate = "2022-07-02T15:22:24.567Z"),
    )

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        activeRecommendation,
        notTheActiveRecommendation,
      ),
    )

    given(recommendationStatusRepository.findByRecommendationId(1234L)).willReturn(
      listOf(
        status("ANOTHER_STATUS", true, 123L),
        status("PP_DOCUMENT_CREATED", true, 123L),
        status("REC_CLOSED", true, 123L),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    then(recommendationStatusRepository).should(times(1)).findByRecommendationId(1234L)
    assertThat(result.results).isEmpty()
  }

  @Test
  fun `do not return results for active recommendation that has not been passed to ppcs`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "Harry",
              surname = "Smith",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        RecommendationEntity(
          id = 123L,
          data = RecommendationModel(crn = "X90902", lastModifiedDate = "2021-07-02T15:22:24.567Z"),
        ),
      ),
    )

    given(recommendationStatusRepository.findByRecommendationId(123L)).willReturn(
      listOf(
        status("NO_DOWNLOADED_YET", true, 123L),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `do not return results for deleted recommendation`() {
    given(offenderSearchApiClient.searchPeople("X90902", null, null, 0, 20)).willReturn(
      Mono.fromCallable {
        OffenderSearchPagedResults(
          content = listOf(
            OffenderDetails(
              firstName = "Harry",
              surname = "Smith",
              dateOfBirth = LocalDate.now(),
              otherIds = OtherIds(
                crn = "X90902",
                nomsNumber = "12345L",
                croNumber = "123/XYZ",
                mostRecentPrisonerNumber = "1234567890",
                pncNumber = "123",
              ),
            ),
          ),
          pageable = PageableResponse(1, 20),
          totalPages = 1,
        )
      },
    )

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        RecommendationEntity(
          id = 123L,
          data = RecommendationModel(crn = "X90902", lastModifiedDate = "2021-07-02T15:22:24.567Z"),
          deleted = true,
        ),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, offenderSearchApiClient)
      .search("X90902")

    then(recommendationStatusRepository).should(times(0)).findByRecommendationId(1234L)
    assertThat(result.results).isEmpty()
  }

  companion object {
    fun status(name: String, active: Boolean, recommendationId: Long): RecommendationStatusEntity {
      return RecommendationStatusEntity(
        id = System.currentTimeMillis(),
        recommendationId = recommendationId,
        createdBy = "me",
        createdByUserFullName = "me2",
        created = ZonedDateTime.now().toString(),
        name = name,
        active = active,
      )
    }
  }
}
