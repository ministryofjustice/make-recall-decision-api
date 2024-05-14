package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentCategory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserRepository
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpudServiceTest : ServiceTestBase() {

  @Mock
  private lateinit var ppudUserRepository: PpudUserRepository

  @Test
  fun `call search`() {
    val request = mock(PpudSearchRequest::class.java)

    val response = mock(PpudSearchResponse::class.java)

    given(ppudAutomationApiClient.search(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).search(request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call details`() {
    val response = mock(PpudDetailsResponse::class.java)

    given(ppudAutomationApiClient.details(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).details("12345678")

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call book to ppud`() {
    val request = mock(PpudBookRecall::class.java)

    val response = mock(PpudBookRecallResponse::class.java)

    given(ppudAutomationApiClient.bookToPpud("123", request)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).bookToPpud("123", request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `reference list`() {
    val response = mock(PpudReferenceListResponse::class.java)

    given(ppudAutomationApiClient.retrieveList("custody-type")).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).retrieveList("custody-type")

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call create offender`() {
    val request = mock(PpudCreateOffenderRequest::class.java)

    val response = mock(PpudCreateOffenderResponse::class.java)

    given(ppudAutomationApiClient.createOffender(request)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).createOffender(request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call update offender`() {
    val request = mock(PpudUpdateOffenderRequest::class.java)

    given(ppudAutomationApiClient.updateOffender("123", request)).willReturn(Mono.empty())

    PpudService(ppudAutomationApiClient, ppudUserRepository).updateOffender("123", request)

    verify(ppudAutomationApiClient).updateOffender("123", request)
  }

  @Test
  fun `call create sentence`() {
    val request = mock(PpudCreateOrUpdateSentenceRequest::class.java)

    val response = mock(PpudCreateSentenceResponse::class.java)

    given(ppudAutomationApiClient.createSentence("123", request)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    PpudService(ppudAutomationApiClient, ppudUserRepository).createSentence("123", request)

    verify(ppudAutomationApiClient).createSentence("123", request)
  }

  @Test
  fun `call update sentence`() {
    val request = mock(PpudCreateOrUpdateSentenceRequest::class.java)

    given(ppudAutomationApiClient.updateSentence("123", "456", request)).willReturn(Mono.empty())

    PpudService(ppudAutomationApiClient, ppudUserRepository).updateSentence("123", "456", request)

    verify(ppudAutomationApiClient).updateSentence("123", "456", request)
  }

  @Test
  fun `call update offence`() {
    val request = mock(PpudUpdateOffenceRequest::class.java)

    given(ppudAutomationApiClient.updateOffence("123", "456", request)).willReturn(Mono.empty())

    PpudService(ppudAutomationApiClient, ppudUserRepository).updateOffence("123", "456", request)

    verify(ppudAutomationApiClient).updateOffence("123", "456", request)
  }

  @Test
  fun `call create or update release`() {
    val request = mock(PpudCreateOrUpdateReleaseRequest::class.java)

    val response = mock(PpudCreateOrUpdateReleaseResponse::class.java)

    given(ppudAutomationApiClient.createOrUpdateRelease("123", "456", request)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).createOrUpdateRelease("123", "456", request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call create recall`() {
    val request = CreateRecallRequest(
      decisionDateTime = LocalDateTime.of(2024, 6, 1, 12, 0),
      isExtendedSentence = false,
      isInCustody = true,
      mappaLevel = "Level 1",
      policeForce = "police force",
      probationArea = "probation area",
      receivedDateTime = LocalDateTime.of(2024, 6, 1, 14, 0),
      riskOfContrabandDetails = "some details",
      riskOfSeriousHarmLevel = RiskOfSeriousHarmLevel.High,
    )

    val response = mock(PpudCreateRecallResponse::class.java)

    val captor = argumentCaptor<PpudCreateRecallRequest>()

    given(ppudUserRepository.findByUserNameIgnoreCase("userId"))
      .willReturn(PpudUserEntity(userName = "userId", ppudUserFullName = "Name", ppudTeamName = "Team"))
    given(ppudAutomationApiClient.createRecall(eq("123"), eq("456"), captor.capture())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient, ppudUserRepository).createRecall("123", "456", request, "userId")

    assertThat(result).isEqualTo(response)

    val ppudCreateRecallRequest = captor.firstValue

    assertThat(ppudCreateRecallRequest.decisionDateTime).isEqualTo(LocalDateTime.of(2024, 6, 1, 13, 0))
    assertThat(ppudCreateRecallRequest.isExtendedSentence).isEqualTo(false)
    assertThat(ppudCreateRecallRequest.isInCustody).isEqualTo(true)
    assertThat(ppudCreateRecallRequest.mappaLevel).isEqualTo("Level 1")
    assertThat(ppudCreateRecallRequest.policeForce).isEqualTo("police force")
    assertThat(ppudCreateRecallRequest.probationArea).isEqualTo("probation area")
    assertThat(ppudCreateRecallRequest.receivedDateTime).isEqualTo(LocalDateTime.of(2024, 6, 1, 15, 0))
    assertThat(ppudCreateRecallRequest.riskOfContrabandDetails).isEqualTo("some details")
    assertThat(ppudCreateRecallRequest.riskOfSeriousHarmLevel).isEqualTo(RiskOfSeriousHarmLevel.High)
  }

  @Test
  fun `create recall throws exception when ppud user not found`() {
    val request = CreateRecallRequest(
      decisionDateTime = LocalDateTime.now(),
      isExtendedSentence = false,
      isInCustody = true,
      mappaLevel = "Level 1",
      policeForce = "police force",
      probationArea = "probation area",
      receivedDateTime = LocalDateTime.of(2024, 1, 1, 14, 0),
      riskOfContrabandDetails = "some details",
      riskOfSeriousHarmLevel = RiskOfSeriousHarmLevel.High,
    )

    val service = PpudService(ppudAutomationApiClient, ppudUserRepository)
    val ex = assertThrows<NotFoundException> {
      service.createRecall("123", "456", request, "userId")
    }
    assertThat(ex.message).isEqualTo("PPUD user not found for username 'userId'")
  }

  @ParameterizedTest
  @CsvSource("PPUDPartA,PartA", "PPUDLicenceDocument,Licence", "PPUDProbationEmail,RecallRequestEmail", "PPUDOASys,OASys", "PPUDPrecons,PreviousConvictions", "PPUDPSR,PreSentenceReport", "PPUDChargeSheet,ChargeSheet")
  fun `upload mandatory document`(category: String, ppudCategory: DocumentCategory) {
    val recallId = "123"
    val request = UploadMandatoryDocumentRequest("12345", category)

    val captor = argumentCaptor<PpudUploadMandatoryDocumentRequest>()

    given(ppudUserRepository.findByUserNameIgnoreCase("userId"))
      .willReturn(PpudUserEntity(userName = "userId", ppudUserFullName = "Name", ppudTeamName = "Team"))

    given(ppudAutomationApiClient.uploadMandatoryDocument(eq("123"), captor.capture())).willReturn(Mono.empty())

    PpudService(ppudAutomationApiClient, ppudUserRepository)
      .uploadMandatoryDocument(recallId = recallId, uploadMandatoryDocument = request, "userId")

    val ppudUploadMandatoryDocumentRequest = captor.firstValue

    assertThat(ppudUploadMandatoryDocumentRequest.id).isEqualTo("12345")
    assertThat(ppudUploadMandatoryDocumentRequest.category).isEqualTo(ppudCategory)
    assertThat(ppudUploadMandatoryDocumentRequest.owningCaseworker).isEqualTo(PpudUser("Name", "Team"))
  }
}
