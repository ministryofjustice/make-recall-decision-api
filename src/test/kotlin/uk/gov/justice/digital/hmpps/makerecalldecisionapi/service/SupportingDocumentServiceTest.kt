package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import java.util.*

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class SupportingDocumentServiceTest {

  @Test
  fun `upload supporting document`() {
    val recommendationRepository = Mockito.mock(RecommendationRepository::class.java)
    val supportingDocumentRepository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val client = Mockito.mock(DocumentManagementClient::class.java)
    val created = DateTimeHelper.utcNowDateTimeString()

    val documentUuid = UUID.randomUUID()
    given(client.uploadFile(any(), any(), any(), any(), any())).willReturn(Mono.just(documentUuid))

    given(recommendationRepository.findById(Mockito.any())).willReturn(Optional.of(RecommendationEntity(id = 1L, data = RecommendationModel(crn = "123"))))
    given(supportingDocumentRepository.save(Mockito.any())).willReturn(
      RecommendationSupportingDocumentEntity(
        id = 456,
        recommendationId = 123,
        title = "title",
        mimetype = "word",
        filename = "word.docx",
        created = created,
        createdBy = "daman",
        createdByUserFullName = "Inspector Morris",
        uploadedBy = null,
        uploadedByUserFullName = null,
        type = "PPUDPartA",
        documentUuid = documentUuid,
      ),
    )

    val id = SupportingDocumentService(supportingDocumentRepository, client, recommendationRepository).uploadNewSupportingDocument(
      recommendationId = 123,
      type = "PPUDPartA",
      title = "title",
      mimetype = "word",
      filename = "word.docx",
      created = created,
      createdBy = "daman",
      createdByUserFullName = "Inspector Morris",
      data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=",
      flags = FeatureFlags(),
    )

    assertThat(id).isEqualTo(456)

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(supportingDocumentRepository).save(captor.capture())
    BDDMockito.then(client).should().uploadFile(any(), any(), any(), any(), any())
    BDDMockito.then(recommendationRepository).should().findById(any())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.documentUuid).isEqualTo(documentUuid)
  }

  @Test
  fun retrieve() {
    val recommendationRepository = Mockito.mock(RecommendationRepository::class.java)
    val supportingDocumentRepository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val client = Mockito.mock(DocumentManagementClient::class.java)

    val created = DateTimeHelper.utcNowDateTimeString()

    given(supportingDocumentRepository.findByRecommendationId(123)).willReturn(
      listOf(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          title = "title",
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
        ),
      ),
    )

    val response = SupportingDocumentService(supportingDocumentRepository, client, recommendationRepository).fetchSupportingDocuments(123)

    assertThat(response.size).isEqualTo(1)

    val entity = response[0]

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")

    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris")
  }

  @Test
  fun replace() {
    val recommendationRepository = Mockito.mock(RecommendationRepository::class.java)
    val supportingDocumentRepository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val client = Mockito.mock(DocumentManagementClient::class.java)
    val documentUuid = UUID.randomUUID()

    given(recommendationRepository.findById(Mockito.any())).willReturn(Optional.of(RecommendationEntity(id = 1L, data = RecommendationModel(crn = "123"))))
    given(client.deleteFile(any())).willReturn(Mono.empty())
    given(client.uploadFile(any(), any(), any(), any(), any())).willReturn(Mono.just(documentUuid))

    val created = DateTimeHelper.utcNowDateTimeString()

    given(supportingDocumentRepository.findById(123)).willReturn(
      Optional.of(
        RecommendationSupportingDocumentEntity(
          id = 1,
          documentUuid = documentUuid,
          recommendationId = 123,
          title = "title",
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
        ),
      ),
    )

    SupportingDocumentService(supportingDocumentRepository, client, recommendationRepository).replaceSupportingDocument(
      123,
      mimetype = "word2",
      title = "title",
      uploadedBy = "daman2",
      uploadedByUserFullName = "Inspector Morris2",
      uploaded = created,
      data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=",
      filename = "word.docx",
      flags = FeatureFlags(),
    )

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(supportingDocumentRepository).save(captor.capture())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word2")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman2")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris2")
  }

  @Test
  fun replace_title() {
    val recommendationRepository = Mockito.mock(RecommendationRepository::class.java)
    val supportingDocumentRepository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val client = Mockito.mock(DocumentManagementClient::class.java)
    val data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM="

    //given(client.deleteFile(Mockito.any())).willReturn(Mono.empty())
    //given(client.uploadFile(any(), any(), any(), any(), any())).willReturn(Mono.just(UUID.randomUUID()))
    //given(recommendationRepository.findById(any())).willReturn(Optional.of(RecommendationEntity(id = 1, data = RecommendationModel(crn = "123"))))
    val created = DateTimeHelper.utcNowDateTimeString()

    given(supportingDocumentRepository.findById(123)).willReturn(
      Optional.of(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          title = "title",
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
          documentUuid = UUID.fromString("61fa04ae-1046-4a63-8249-b04f42620d07"),
        ),
      ),
    )

    SupportingDocumentService(supportingDocumentRepository, client, recommendationRepository).replaceSupportingDocument(
      123,
      mimetype = null, //"word",
      title = "title 2",
      uploadedBy = "daman2",
      uploadedByUserFullName = "Inspector Morris2",
      uploaded = created,
      data = null,//data,
      filename = null,//"word.docx",
      flags = FeatureFlags(),
    )

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(supportingDocumentRepository).save(captor.capture())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.title).isEqualTo("title 2")
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman2")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris2")
    // RL This next line isn't necessary if we shouldn't be storing the doc in CaR
    // assertThat(String(entity.data!!)).isEqualTo("The hills are alive with thet sound of music")
    assertThat(entity.data).isNull()
  }

  @Test
  fun `get`() {
    val recommendationRepository = Mockito.mock(RecommendationRepository::class.java)
    val supportingDocumentRepository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val client = Mockito.mock(DocumentManagementClient::class.java)

    given(client.downloadFileAsByteArray(Mockito.any(), Mockito.any()))
      .willReturn(Mono.just("The hills are alive with thet sound of music".encodeToByteArray()))

    val created = DateTimeHelper.utcNowDateTimeString()

    given(supportingDocumentRepository.findById(123)).willReturn(
      Optional.of(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          title = "title",
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
          data = "The hills are alive with thet sound of music".encodeToByteArray(),
        ),
      ),
    )

    val entity = SupportingDocumentService(supportingDocumentRepository, client, recommendationRepository).getSupportingDocument(123, FeatureFlags())

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.data).isEqualTo("VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=")
  }
}
