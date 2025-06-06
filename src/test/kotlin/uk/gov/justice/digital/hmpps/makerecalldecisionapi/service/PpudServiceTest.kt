package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ppud.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentCategory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateMinuteRequest
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpudServiceTest : ServiceTestBase() {

  @InjectMocks
  private lateinit var ppudService: PpudService

  @Mock
  private lateinit var ppudAutomationApiClient: PpudAutomationApiClient

  @Mock
  private lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @Mock
  private lateinit var recommendationDocumentRepository: RecommendationSupportingDocumentRepository

  @Test
  fun `call search`() {
    val request = mock(PpudSearchRequest::class.java)

    val response = mock(PpudSearchResponse::class.java)

    given(ppudAutomationApiClient.search(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = ppudService.search(request)

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

    val result = ppudService.details("12345678")

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

    val result = ppudService.retrieveList("custody-type")

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call create offender`() {
    val request = mock(PpudCreateOffenderRequest::class.java)

    val response = mock(PpudCreateOffenderResponse::class.java)

    given(ppudAutomationApiClient.createOffender(request)).willReturn(Mono.just(response))

    val result = ppudService.createOffender(request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call update offender`() {
    val offenderId = "123"
    val request = mock(PpudUpdateOffenderRequest::class.java)
    given(ppudAutomationApiClient.updateOffender(offenderId, request)).willReturn(Mono.empty())

    ppudService.updateOffender(offenderId, request)

    verify(ppudAutomationApiClient).updateOffender(offenderId, request)
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

    ppudService.createSentence("123", request)

    verify(ppudAutomationApiClient).createSentence("123", request)
  }

  @Test
  fun `call update sentence`() {
    val request = mock(PpudCreateOrUpdateSentenceRequest::class.java)

    given(ppudAutomationApiClient.updateSentence("123", "456", request)).willReturn(Mono.empty())

    ppudService.updateSentence("123", "456", request)

    verify(ppudAutomationApiClient).updateSentence("123", "456", request)
  }

  @Test
  fun `call update offence`() {
    val request = mock(PpudUpdateOffenceRequest::class.java)

    given(ppudAutomationApiClient.updateOffence("123", "456", request)).willReturn(Mono.empty())

    ppudService.updateOffence("123", "456", request)

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

    val result = ppudService.createOrUpdateRelease("123", "456", request)

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
    )

    val response = mock(PpudCreateRecallResponse::class.java)

    val captor = argumentCaptor<PpudCreateRecallRequest>()

    given(ppudUserMappingRepository.findByUserNameIgnoreCase("userId"))
      .willReturn(PpudUserMappingEntity(userName = "userId", ppudUserFullName = "Name", ppudTeamName = "Team", ppudUserName = "UserName"))

    given(ppudAutomationApiClient.createRecall(eq("123"), eq("456"), captor.capture())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = ppudService.createRecall("123", "456", request, "userId")

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
  }

  @Test
  fun `create recall throws exception when ppud user mapping not found`() {
    val request = CreateRecallRequest(
      decisionDateTime = LocalDateTime.now(),
      isExtendedSentence = false,
      isInCustody = true,
      mappaLevel = "Level 1",
      policeForce = "police force",
      probationArea = "probation area",
      receivedDateTime = LocalDateTime.of(2024, 1, 1, 14, 0),
      riskOfContrabandDetails = "some details",
    )

    val ex = assertThrows<NotFoundException> {
      ppudService.createRecall("123", "456", request, "userId")
    }
    assertThat(ex.message).isEqualTo("PPUD user mapping not found for username 'userId'")
  }

  @ParameterizedTest
  @CsvSource(
    "PPUDPartA,PartA",
    "PPUDLicenceDocument,Licence",
    "PPUDProbationEmail,RecallRequestEmail",
    "PPUDOASys,OASys",
    "PPUDPrecons,PreviousConvictions",
    "PPUDPSR,PreSentenceReport",
    "PPUDChargeSheet,ChargeSheet",
  )
  fun `upload mandatory document`(category: String, ppudCategory: DocumentCategory) {
    val recallId = "123"
    val request = UploadMandatoryDocumentRequest(12345, category)
    val documentId = UUID.randomUUID()

    val captor = argumentCaptor<PpudUploadMandatoryDocumentRequest>()

    given(ppudUserMappingRepository.findByUserNameIgnoreCase("userId"))
      .willReturn(PpudUserMappingEntity(userName = "userId", ppudUserFullName = "Name", ppudTeamName = "Team", ppudUserName = "UserName"))

    given(recommendationDocumentRepository.findById(12345))
      .willReturn(
        Optional.of(
          RecommendationSupportingDocumentEntity(
            id = 456,
            created = null,
            createdBy = null,
            createdByUserFullName = null,
            data = ByteArray(0),
            mimetype = null,
            filename = "",
            title = "",
            type = "",
            recommendationId = 123,
            documentUuid = documentId,
          ),
        ),
      )

    given(ppudAutomationApiClient.uploadMandatoryDocument(eq("123"), captor.capture())).willReturn(Mono.empty())

    ppudService.uploadMandatoryDocument(recallId = recallId, uploadMandatoryDocument = request, "userId")

    val ppudUploadMandatoryDocumentRequest = captor.firstValue

    assertThat(ppudUploadMandatoryDocumentRequest.documentId).isEqualTo(documentId)
    assertThat(ppudUploadMandatoryDocumentRequest.category).isEqualTo(ppudCategory)
    assertThat(ppudUploadMandatoryDocumentRequest.owningCaseworker).isEqualTo(PpudUser("Name", "Team"))
  }

  @Test
  fun `upload additional document`() {
    val recallId = "123"
    val request = UploadAdditionalDocumentRequest(12345)
    val documentId = UUID.randomUUID()

    val captor = argumentCaptor<PpudUploadAdditionalDocumentRequest>()

    given(ppudUserMappingRepository.findByUserNameIgnoreCase("userId"))
      .willReturn(PpudUserMappingEntity(userName = "userId", ppudUserFullName = "Name", ppudTeamName = "Team", ppudUserName = "UserName"))

    given(recommendationDocumentRepository.findById(12345))
      .willReturn(
        Optional.of(
          RecommendationSupportingDocumentEntity(
            id = 456,
            created = null,
            createdBy = null,
            createdByUserFullName = null,
            data = ByteArray(0),
            mimetype = null,
            filename = "",
            title = "some title",
            type = "",
            recommendationId = 123,
            documentUuid = documentId,
          ),
        ),
      )

    given(ppudAutomationApiClient.uploadAdditionalDocument(eq("123"), captor.capture())).willReturn(Mono.empty())

    ppudService.uploadAdditionalDocument(recallId = recallId, uploadMandatoryDocument = request, "userId")

    val ppudUploadMandatoryDocumentRequest = captor.firstValue

    assertThat(ppudUploadMandatoryDocumentRequest.documentId).isEqualTo(documentId)
    assertThat(ppudUploadMandatoryDocumentRequest.title).isEqualTo("some title")
    assertThat(ppudUploadMandatoryDocumentRequest.owningCaseworker).isEqualTo(PpudUser("Name", "Team"))
  }

  @Test
  fun `call create minute`() {
    given(ppudAutomationApiClient.createMinute("123", PpudCreateMinuteRequest("some subject", "some text"))).willReturn(
      Mono.empty(),
    )

    ppudService.createMinute("123", CreateMinuteRequest("some subject", "some text"), "username")

    verify(ppudAutomationApiClient).createMinute("123", PpudCreateMinuteRequest("some subject", "some text"))
  }
}
