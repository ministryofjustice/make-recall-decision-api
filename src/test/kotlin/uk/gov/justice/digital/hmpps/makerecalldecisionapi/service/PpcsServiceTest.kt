package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Name
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpcsServiceTest : ServiceTestBase() {

  @Test
  fun `excluded records won't be returned`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(excludedAccess())

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    assertThat(result.results).isEmpty()
    verifyNoInteractions(recommendationRepository)
  }

  @Test
  fun `no case record`() {
    given(deliusClient.findByCrn("X90902")).willReturn(null)

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `single active document for ppcs`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(noAccessLimitations())

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

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    assertThat(result.results).isNotEmpty
  }

  @Test
  fun `do not return results for active recommendation if already been booked`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(noAccessLimitations())

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

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `consider active recommendation only for ppcs search`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(noAccessLimitations())

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

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    then(recommendationStatusRepository).should(times(1)).findByRecommendationId(1234L)
    assertThat(result.results).isEmpty()
  }

  @Test
  fun `do not return results for active recommendation that has not been passed to ppcs`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(noAccessLimitations())

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

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    assertThat(result.results).isEmpty()
  }

  @Test
  fun `do not return results for deleted recommendation`() {
    given(deliusClient.findByCrn("X90902")).willReturn(
      DeliusClient.PersonalDetailsOverview(
        name = Name(
          forename = "Harry",
          middleName = null,
          surname = "Smith",
        ),
        dateOfBirth = LocalDate.now(),
        identifiers = DeliusClient.PersonalDetailsOverview.Identifiers(
          crn = "X90902",
          nomsNumber = "12345L",
          croNumber = "123/XYZ",
          bookingNumber = "1234567890",
          pncNumber = "123",
        ),
        gender = "Male",
        ethnicity = "test",
        primaryLanguage = "test",
      ),
    )
    given(deliusClient.getUserAccess(username, "X90902")).willReturn(noAccessLimitations())

    given(recommendationRepository.findByCrn("X90902")).willReturn(
      listOf(
        RecommendationEntity(
          id = 123L,
          data = RecommendationModel(crn = "X90902", lastModifiedDate = "2021-07-02T15:22:24.567Z"),
          deleted = true,
        ),
      ),
    )

    val result = PpcsService(recommendationRepository, recommendationStatusRepository, deliusClient, userAccessValidator)
      .search("X90902")

    then(recommendationStatusRepository).should(times(0)).findByRecommendationId(1234L)
    assertThat(result.results).isEmpty()
  }

  companion object {
    fun status(name: String, active: Boolean, recommendationId: Long): RecommendationStatusEntity = RecommendationStatusEntity(
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
